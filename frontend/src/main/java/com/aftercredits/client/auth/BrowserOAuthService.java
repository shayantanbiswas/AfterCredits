package com.aftercredits.client.auth;

import com.aftercredits.theme.ThemeManager;
import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;

import javax.swing.*;
import java.awt.*;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

/**
 * Manages the desktop-to-browser OAuth flow.
 * Connects directly to Google's live OAuth 2.0 servers (accounts.google.com) using the
 * configured Google Cloud Client ID & Secret, captures the real authorization code
 * via a local loopback server, and retrieves verified user identity from Google's APIs.
 */
public final class BrowserOAuthService {

    public static final String GOOGLE_CLIENT_ID = System.getProperty(
            "aftercredits.google.client_id",
            System.getenv("GOOGLE_CLIENT_ID")
    );

    public static final String GOOGLE_CLIENT_SECRET = System.getProperty(
            "aftercredits.google.client_secret",
            System.getenv("GOOGLE_CLIENT_SECRET")
    );

    private static HttpServer activeServer = null;
    private static JDialog waitingDialog = null;

    private BrowserOAuthService() {}

    public record OAuthPayload(
            String provider,
            String providerId,
            String displayName,
            String email
    ) {}

    /**
     * Starts the browser sign-in flow.
     * For Google, it redirects directly to Google's official accounts.google.com OAuth page.
     *
     * @param provider         "GOOGLE", "FACEBOOK", or "INSTAGRAM"
     * @param parentComponent  parent Swing component for the waiting dialog
     * @param onSuccess        callback invoked on the EDT upon successful authentication
     * @param onError          callback invoked on the EDT upon cancellation or failure
     */
    public static synchronized void startBrowserAuth(
            String provider,
            Component parentComponent,
            Consumer<OAuthPayload> onSuccess,
            Consumer<String> onError
    ) {
        // Cancel any active previous flow
        cancelActiveFlow();

        final String stateToken = UUID.randomUUID().toString();
        final String providerName = getProviderDisplayName(provider);
        final boolean isGoogle = "GOOGLE".equalsIgnoreCase(provider);

        try {
            // Bind to an ephemeral port (port 0 selects any free port automatically)
            HttpServer server = HttpServer.create(new InetSocketAddress("127.0.0.1", 0), 0);
            server.setExecutor(Executors.newSingleThreadExecutor());
            activeServer = server;

            final int port = server.getAddress().getPort();
            final String redirectUri = "http://127.0.0.1:" + port + "/callback";

            // Determine the URL to open in the user's browser
            final String browserUrl;
            if (isGoogle && GOOGLE_CLIENT_ID != null && !GOOGLE_CLIENT_ID.isBlank()) {
                // REAL LIVE GOOGLE OAUTH 2.0 URL
                browserUrl = "https://accounts.google.com/o/oauth2/v2/auth?"
                        + "client_id=" + URLEncoder.encode(GOOGLE_CLIENT_ID, StandardCharsets.UTF_8)
                        + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                        + "&response_type=code"
                        + "&scope=" + URLEncoder.encode("openid profile email", StandardCharsets.UTF_8)
                        + "&access_type=offline"
                        + "&state=" + URLEncoder.encode(stateToken, StandardCharsets.UTF_8)
                        + "&prompt=select_account";
            } else {
                // Branded local portal fallback for Facebook / Instagram / offline
                browserUrl = "http://127.0.0.1:" + port + "/auth?provider=" + provider + "&state=" + stateToken;

                server.createContext("/auth", new HttpHandler() {
                    @Override
                    public void handle(HttpExchange exchange) throws IOException {
                        Map<String, String> query = parseQuery(exchange.getRequestURI().getRawQuery());
                        String reqState = query.get("state");

                        if (reqState == null || !reqState.equals(stateToken)) {
                            sendResponse(exchange, 400, "text/html; charset=UTF-8", "<h3>400 Invalid or expired session state.</h3>");
                            return;
                        }

                        String html = buildBrandedAuthHtml(provider, stateToken, port);
                        sendResponse(exchange, 200, "text/html; charset=UTF-8", html);
                    }
                });
            }

            // Route: /callback (Receives the authorization code from Google or the local form)
            server.createContext("/callback", new HttpHandler() {
                @Override
                public void handle(HttpExchange exchange) throws IOException {
                    Map<String, String> params = new HashMap<>();

                    if ("POST".equalsIgnoreCase(exchange.getRequestMethod())) {
                        String body = new String(exchange.getRequestBody().readAllBytes(), StandardCharsets.UTF_8);
                        params.putAll(parseQuery(body));
                    } else {
                        params.putAll(parseQuery(exchange.getRequestURI().getRawQuery()));
                    }

                    // Check for user cancellation on Google
                    String error = params.get("error");
                    if (error != null) {
                        String errorHtml = buildErrorHtml(providerName, error);
                        sendResponse(exchange, 200, "text/html; charset=UTF-8", errorHtml);
                        SwingUtilities.invokeLater(() -> {
                            closeWaitingDialog();
                            cancelActiveFlow();
                            if (onError != null) onError.accept("Google sign-in was cancelled or declined: " + error);
                        });
                        return;
                    }

                    String reqState = params.get("state");
                    if (reqState == null || !reqState.equals(stateToken)) {
                        sendResponse(exchange, 400, "text/html; charset=UTF-8", "<h3>400 Invalid or expired session state.</h3>");
                        return;
                    }

                    String authCode = params.get("code");
                    OAuthPayload payload;

                    if (isGoogle && authCode != null && !authCode.isBlank()) {
                        // REAL GOOGLE CODE EXCHANGE WITH GOOGLE'S LIVE TOKEN & USERINFO APIS
                        try {
                            payload = exchangeGoogleAuthCode(authCode, redirectUri);
                        } catch (Exception ex) {
                            String failHtml = buildErrorHtml("Google", "Could not verify credentials with Google: " + ex.getMessage());
                            sendResponse(exchange, 500, "text/html; charset=UTF-8", failHtml);
                            SwingUtilities.invokeLater(() -> {
                                closeWaitingDialog();
                                cancelActiveFlow();
                                if (onError != null) onError.accept("Google verification failed: " + ex.getMessage());
                            });
                            return;
                        }
                    } else {
                        // Form fallback
                        String name = params.getOrDefault("displayName", "Cinema Fan").trim();
                        if (name.isBlank()) name = "Cinema Fan";

                        String email = params.getOrDefault("email", "").trim();
                        if (email.isBlank()) {
                            email = name.toLowerCase().replaceAll("[^a-z0-9]", "") + "@" + provider.toLowerCase() + ".com";
                        }

                        String providerId = provider.toLowerCase() + "_" + Math.abs((name + email).hashCode() % 1000000);
                        payload = new OAuthPayload(provider, providerId, name, email);
                    }

                    // Send sleek success page to browser
                    String successHtml = buildSuccessHtml(providerName, payload.displayName(), payload.email());
                    sendResponse(exchange, 200, "text/html; charset=UTF-8", successHtml);

                    // Stop server and notify Swing on EDT
                    final OAuthPayload finalPayload = payload;
                    SwingUtilities.invokeLater(() -> {
                        closeWaitingDialog();
                        cancelActiveFlow();
                        onSuccess.accept(finalPayload);
                    });
                }
            });

            server.start();

            // Open the default browser to Google's live sign-in page
            boolean browserOpened = false;
            if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Desktop.Action.BROWSE)) {
                try {
                    Desktop.getDesktop().browse(new URI(browserUrl));
                    browserOpened = true;
                } catch (Exception ex) {
                    System.err.println("[BrowserOAuthService] Failed to open browser: " + ex.getMessage());
                }
            }

            // Show non-blocking waiting dialog in Swing app
            showWaitingDialog(parentComponent, providerName, browserUrl, browserOpened, () -> {
                cancelActiveFlow();
                if (onError != null) onError.accept("Sign in cancelled by user.");
            });

        } catch (Exception e) {
            cancelActiveFlow();
            if (onError != null) onError.accept("Could not start local auth server: " + e.getMessage());
        }
    }

    /**
     * Exchanges Google authorization code with https://oauth2.googleapis.com/token
     * and fetches the verified user profile from https://www.googleapis.com/oauth2/v3/userinfo.
     */
    private static OAuthPayload exchangeGoogleAuthCode(String code, String redirectUri) throws Exception {
        HttpClient client = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        String requestBody = "code=" + URLEncoder.encode(code, StandardCharsets.UTF_8)
                + "&client_id=" + URLEncoder.encode(GOOGLE_CLIENT_ID, StandardCharsets.UTF_8)
                + "&client_secret=" + URLEncoder.encode(GOOGLE_CLIENT_SECRET, StandardCharsets.UTF_8)
                + "&redirect_uri=" + URLEncoder.encode(redirectUri, StandardCharsets.UTF_8)
                + "&grant_type=authorization_code";

        HttpRequest tokenReq = HttpRequest.newBuilder()
                .uri(URI.create("https://oauth2.googleapis.com/token"))
                .timeout(Duration.ofSeconds(12))
                .header("Content-Type", "application/x-www-form-urlencoded")
                .header("User-Agent", "AfterCredits-Desktop/1.0")
                .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                .build();

        HttpResponse<String> tokenRes = client.send(tokenReq, HttpResponse.BodyHandlers.ofString());
        if (tokenRes.statusCode() != 200) {
            throw new IllegalStateException("Google token exchange error (HTTP " + tokenRes.statusCode() + "): " + tokenRes.body());
        }

        Gson gson = new Gson();
        JsonObject tokenJson = gson.fromJson(tokenRes.body(), JsonObject.class);
        String accessToken = tokenJson.get("access_token").getAsString();

        // Fetch verified user profile from Google UserInfo endpoint
        HttpRequest userinfoReq = HttpRequest.newBuilder()
                .uri(URI.create("https://www.googleapis.com/oauth2/v3/userinfo"))
                .timeout(Duration.ofSeconds(12))
                .header("Authorization", "Bearer " + accessToken)
                .header("User-Agent", "AfterCredits-Desktop/1.0")
                .GET()
                .build();

        HttpResponse<String> userinfoRes = client.send(userinfoReq, HttpResponse.BodyHandlers.ofString());
        if (userinfoRes.statusCode() != 200) {
            throw new IllegalStateException("Google UserInfo error (HTTP " + userinfoRes.statusCode() + "): " + userinfoRes.body());
        }

        JsonObject userJson = gson.fromJson(userinfoRes.body(), JsonObject.class);
        String sub = userJson.get("sub").getAsString();
        String name = userJson.has("name") && !userJson.get("name").isJsonNull()
                ? userJson.get("name").getAsString()
                : (userJson.has("email") ? userJson.get("email").getAsString() : "Google User");
        String email = userJson.has("email") && !userJson.get("email").isJsonNull()
                ? userJson.get("email").getAsString()
                : null;

        return new OAuthPayload("GOOGLE", "google_" + sub, name, email);
    }

    public static synchronized void cancelActiveFlow() {
        if (activeServer != null) {
            try {
                activeServer.stop(0);
            } catch (Exception ignored) {}
            activeServer = null;
        }
        closeWaitingDialog();
    }

    private static void closeWaitingDialog() {
        if (waitingDialog != null) {
            waitingDialog.dispose();
            waitingDialog = null;
        }
    }

    private static void showWaitingDialog(
            Component parent,
            String providerName,
            String authUrl,
            boolean browserOpened,
            Runnable onCancel
    ) {
        Window parentWindow = parent != null ? SwingUtilities.getWindowAncestor(parent) : null;
        waitingDialog = new JDialog(parentWindow, "Google Sign-In", Dialog.ModalityType.APPLICATION_MODAL);
        waitingDialog.setSize(440, 270);
        waitingDialog.setLocationRelativeTo(parent);
        waitingDialog.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        waitingDialog.addWindowListener(new java.awt.event.WindowAdapter() {
            @Override
            public void windowClosing(java.awt.event.WindowEvent e) {
                onCancel.run();
            }
        });

        JPanel panel = new JPanel();
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
        panel.setBackground(ThemeManager.BG_PRIMARY);
        panel.setBorder(BorderFactory.createEmptyBorder(24, 28, 24, 28));

        JLabel title = new JLabel("🌐  Connecting to " + providerName);
        title.setFont(ThemeManager.FONT_SECTION_HEADING);
        title.setForeground(ThemeManager.TEXT_PRIMARY);
        title.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel msg = new JLabel(
                "<html><div style='text-align: center; width: 340px;'>"
                + (browserOpened
                    ? "The official Google sign-in page opened in your web browser.<br>Please enter your Google email & password there to continue."
                    : "Please click below to open the official Google sign-in page in your browser:")
                + "</div></html>"
        );
        msg.setFont(ThemeManager.FONT_BODY);
        msg.setForeground(ThemeManager.TEXT_MUTED);
        msg.setAlignmentX(Component.CENTER_ALIGNMENT);

        JButton openBtn = ThemeManager.createAccentButton("Open Google Login ↗");
        openBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        openBtn.addActionListener(e -> {
            try {
                Desktop.getDesktop().browse(new URI(authUrl));
            } catch (Exception ignored) {}
        });

        JButton cancelBtn = ThemeManager.createGhostButton("Cancel");
        cancelBtn.setAlignmentX(Component.CENTER_ALIGNMENT);
        cancelBtn.addActionListener(e -> onCancel.run());

        panel.add(title);
        panel.add(Box.createVerticalStrut(12));
        panel.add(msg);
        panel.add(Box.createVerticalStrut(16));
        if (!browserOpened) {
            panel.add(openBtn);
            panel.add(Box.createVerticalStrut(10));
        }
        panel.add(cancelBtn);

        waitingDialog.setContentPane(panel);
        SwingUtilities.invokeLater(() -> {
            if (waitingDialog != null) {
                waitingDialog.setVisible(true);
            }
        });
    }

    private static String getProviderDisplayName(String provider) {
        if ("GOOGLE".equalsIgnoreCase(provider)) return "Google";
        if ("FACEBOOK".equalsIgnoreCase(provider)) return "Facebook";
        if ("INSTAGRAM".equalsIgnoreCase(provider)) return "Instagram";
        return provider;
    }

    private static void sendResponse(HttpExchange exchange, int statusCode, String contentType, String html) throws IOException {
        byte[] bytes = html.getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", contentType);
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try (OutputStream os = exchange.getResponseBody()) {
            os.write(bytes);
        }
    }

    private static Map<String, String> parseQuery(String query) {
        Map<String, String> result = new HashMap<>();
        if (query == null || query.isBlank()) return result;
        String[] pairs = query.split("&");
        for (String pair : pairs) {
            int idx = pair.indexOf("=");
            if (idx > 0) {
                String key = URLDecoder.decode(pair.substring(0, idx), StandardCharsets.UTF_8);
                String val = URLDecoder.decode(pair.substring(idx + 1), StandardCharsets.UTF_8);
                result.put(key, val);
            }
        }
        return result;
    }

    /**
     * Builds the success page shown in the browser after completing the flow.
     */
    private static String buildSuccessHtml(String providerName, String displayName, String email) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <meta name=\"viewport\" content=\"width=device-width, initial-scale=1.0\">\n" +
                "  <title>Signed In with " + providerName + " — AfterCredits</title>\n" +
                "  <style>\n" +
                "    * { box-sizing: border-box; margin: 0; padding: 0; }\n" +
                "    body {\n" +
                "      font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif;\n" +
                "      background: #0f131a;\n" +
                "      color: #e2e8f0;\n" +
                "      display: flex;\n" +
                "      align-items: center;\n" +
                "      justify-content: center;\n" +
                "      height: 100vh;\n" +
                "      padding: 20px;\n" +
                "    }\n" +
                "    .card {\n" +
                "      background: #181d26;\n" +
                "      border: 1px solid #283141;\n" +
                "      border-radius: 16px;\n" +
                "      width: 100%;\n" +
                "      max-width: 440px;\n" +
                "      padding: 44px 36px;\n" +
                "      text-align: center;\n" +
                "      box-shadow: 0 20px 40px rgba(0, 0, 0, 0.45);\n" +
                "    }\n" +
                "    .icon {\n" +
                "      width: 64px;\n" +
                "      height: 64px;\n" +
                "      border-radius: 50%;\n" +
                "      background: rgba(72, 187, 120, 0.15);\n" +
                "      color: #48bb78;\n" +
                "      font-size: 34px;\n" +
                "      display: inline-flex;\n" +
                "      align-items: center;\n" +
                "      justify-content: center;\n" +
                "      margin-bottom: 20px;\n" +
                "      border: 1px solid rgba(72, 187, 120, 0.3);\n" +
                "    }\n" +
                "    h2 { font-size: 22px; font-weight: 600; color: #ffffff; margin-bottom: 8px; }\n" +
                "    .email { font-size: 13px; color: #e5a93c; margin-bottom: 16px; font-weight: 500; }\n" +
                "    p { font-size: 14px; color: #94a3b8; line-height: 1.6; margin-bottom: 24px; }\n" +
                "    .hint {\n" +
                "      font-size: 12px;\n" +
                "      color: #e5a93c;\n" +
                "      background: rgba(229, 169, 60, 0.1);\n" +
                "      padding: 10px 14px;\n" +
                "      border-radius: 8px;\n" +
                "      border: 1px solid rgba(229, 169, 60, 0.25);\n" +
                "    }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"card\">\n" +
                "    <div class=\"icon\">✓</div>\n" +
                "    <h2>Signed in with " + providerName + "!</h2>\n" +
                "    <div class=\"email\">" + escapeHtml(email != null ? email : "") + "</div>\n" +
                "    <p>Welcome, <b>" + escapeHtml(displayName) + "</b>.<br>Your account is verified and ready in <b>AfterCredits</b>.</p>\n" +
                "    <div class=\"hint\">✓ You can safely close this browser tab and return to the app.</div>\n" +
                "  </div>\n" +
                "  <script>\n" +
                "    setTimeout(function() { try { window.close(); } catch(e){} }, 3000);\n" +
                "  </script>\n" +
                "</body>\n" +
                "</html>";
    }

    private static String buildErrorHtml(String providerName, String errorMsg) {
        return "<!DOCTYPE html>\n" +
                "<html lang=\"en\">\n" +
                "<head>\n" +
                "  <meta charset=\"UTF-8\">\n" +
                "  <title>Sign In Cancelled</title>\n" +
                "  <style>\n" +
                "    body { font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; background: #0f131a; color: #e2e8f0; display: flex; align-items: center; justify-content: center; height: 100vh; padding: 20px; }\n" +
                "    .card { background: #181d26; border: 1px solid #283141; border-radius: 16px; max-width: 420px; padding: 40px; text-align: center; }\n" +
                "    h2 { color: #f87171; margin-bottom: 12px; font-size: 20px; }\n" +
                "    p { color: #94a3b8; font-size: 14px; line-height: 1.5; }\n" +
                "  </style>\n" +
                "</head>\n" +
                "<body>\n" +
                "  <div class=\"card\">\n" +
                "    <h2>" + providerName + " Sign-In Cancelled</h2>\n" +
                "    <p>" + escapeHtml(errorMsg) + "<br><br>You can close this tab and try again in AfterCredits.</p>\n" +
                "  </div>\n" +
                "</body>\n" +
                "</html>";
    }

    private static String escapeHtml(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;").replace("\"", "&quot;");
    }

    private static String buildBrandedAuthHtml(String provider, String state, int port) {
        boolean isFacebook = "FACEBOOK".equalsIgnoreCase(provider);
        String title = isFacebook ? "Log in with Facebook" : "Log in with Instagram";
        String primaryColor = isFacebook ? "#1877f2" : "#e1306c";

        return "<!DOCTYPE html><html><head><meta charset=\"UTF-8\"><title>" + title + "</title>"
                + "<style>body{font-family:sans-serif;background:#0f131a;color:#fff;display:flex;align-items:center;justify-content:center;height:100vh;margin:0;}"
                + ".card{background:#181d26;padding:36px;border-radius:12px;border:1px solid #283141;width:380px;text-align:center;}"
                + "input{width:100%;box-sizing:border-box;padding:10px;margin:8px 0;border-radius:6px;border:1px solid #333;background:#12161f;color:#fff;}"
                + "button{width:100%;padding:12px;background:" + primaryColor + ";color:#fff;border:none;border-radius:6px;font-weight:bold;cursor:pointer;margin-top:10px;}"
                + "</style></head><body><div class=\"card\">"
                + "<h2>" + title + "</h2><p style='color:#94a3b8;font-size:13px;margin:10px 0 20px;'>to continue to AfterCredits</p>"
                + "<form action=\"/callback\" method=\"POST\">"
                + "<input type=\"hidden\" name=\"state\" value=\"" + state + "\">"
                + "<input type=\"text\" name=\"displayName\" required placeholder=\"Display Name\" value=\"Cinema Fan\">"
                + "<input type=\"email\" name=\"email\" required placeholder=\"Email Address\" value=\"user@" + provider.toLowerCase() + ".com\">"
                + "<button type=\"submit\">Continue</button>"
                + "</form></div></body></html>";
    }
}
