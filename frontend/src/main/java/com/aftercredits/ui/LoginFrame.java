package com.aftercredits.ui;

import com.aftercredits.client.auth.AuthApiClient;
import com.aftercredits.client.auth.BrowserOAuthService;
import com.aftercredits.client.auth.SessionManager;
import com.aftercredits.repository.CinemaRepository;
import com.aftercredits.ui.components.CinemaLogo;
import com.aftercredits.theme.ThemeManager;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.function.Consumer;

/**
 * Cinematic entry screen for AfterCredits.
 * Supports verified Login with Email/Username & Password, Sign-Up with Username/Email/Password,
 * Social Auth (Google / Facebook / Instagram), and Guest access with auto-generated Guest ID.
 * Strictly verifies passwords against registered accounts and never bypasses authentication.
 */
public class LoginFrame extends JFrame {

    // Panel modes
    private static final String PANEL_LOGIN  = "LOGIN";
    private static final String PANEL_SIGNUP = "SIGNUP";

    private final CinemaRepository repository;
    private final Runnable onLogin;

    // Login panel fields
    private JTextField loginIdentifierField;
    private JPasswordField loginPasswordField;

    // Sign-up panel fields
    private JTextField signupUsernameField;
    private JTextField signupEmailField;
    private JPasswordField signupPasswordField;
    private JPasswordField signupConfirmPasswordField;

    // CardLayout host
    private JPanel cardHost;
    private CardLayout cardLayout;

    // Outer animated card
    private JPanel card;
    private float animationProgress = 0f;

    public LoginFrame(CinemaRepository repository, Runnable onLogin) {
        super("AfterCredits — Social Cinema Platform");
        this.repository = repository;
        this.onLogin = onLogin;

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setMinimumSize(new Dimension(1000, 680));
        setSize(1200, 840);
        setLocationRelativeTo(null);

        buildUI();
        startEntranceAnimation();
    }

    // ── UI Construction ──────────────────────────────────────────────

    private void buildUI() {
        CinematicBackground background = new CinematicBackground();
        background.setLayout(new GridBagLayout());

        JPanel content = new JPanel();
        content.setOpaque(false);
        content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));

        CinemaLogo logo = new CinemaLogo(58);
        logo.setAlignmentX(Component.CENTER_ALIGNMENT);

        JLabel title = new JLabel("AFTERCREDITS");
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        title.setForeground(Color.WHITE);
        title.setFont(new Font("SansSerif", Font.BOLD, 30));

        JLabel subtitle = new JLabel("YOUR CINEMA. YOUR TASTE. YOUR STORY.");
        subtitle.setAlignmentX(Component.CENTER_ALIGNMENT);
        subtitle.setForeground(ThemeManager.TEXT_MUTED);
        subtitle.setFont(new Font("SansSerif", Font.PLAIN, 11));

        content.add(logo);
        content.add(Box.createVerticalStrut(4));
        content.add(title);
        content.add(Box.createVerticalStrut(6));
        content.add(subtitle);
        content.add(Box.createVerticalStrut(24));

        // Outer animated card
        card = new JPanel();
        card.setOpaque(true);
        card.setBackground(new Color(16, 19, 25, 245));
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR),
                new EmptyBorder(26, 32, 26, 32)
        ));
        card.setLayout(new BoxLayout(card, BoxLayout.Y_AXIS));
        card.setAlignmentX(Component.CENTER_ALIGNMENT);
        card.setPreferredSize(new Dimension(410, 560));
        card.setMaximumSize(new Dimension(410, 560));

        // CardLayout host inside the outer card
        cardLayout = new CardLayout();
        cardHost = new JPanel(cardLayout);
        cardHost.setOpaque(false);

        cardHost.add(buildLoginPanel(), PANEL_LOGIN);
        cardHost.add(buildSignupPanel(), PANEL_SIGNUP);

        card.add(cardHost);
        content.add(card);
        content.add(Box.createVerticalStrut(16));

        JLabel footer = new JLabel("A social cinema experience by AfterCredits");
        footer.setAlignmentX(Component.CENTER_ALIGNMENT);
        footer.setForeground(ThemeManager.TEXT_FAINT);
        footer.setFont(new Font("SansSerif", Font.PLAIN, 11));
        content.add(footer);

        GridBagConstraints gbc = new GridBagConstraints();
        gbc.gridx = 0;
        gbc.gridy = 0;
        gbc.anchor = GridBagConstraints.CENTER;

        background.add(content, gbc);
        setContentPane(background);
    }

    private JPanel buildLoginPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel welcome = new JLabel("Welcome back");
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel hint = new JLabel("Sign in with verified account credentials");
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setForeground(ThemeManager.TEXT_MUTED);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));

        panel.add(welcome);
        panel.add(Box.createVerticalStrut(4));
        panel.add(hint);
        panel.add(Box.createVerticalStrut(14));

        // Social login buttons (Google / Facebook / Instagram)
        panel.add(buildSocialRow(this::handleSocialLogin));
        panel.add(Box.createVerticalStrut(10));

        // Divider
        panel.add(buildDivider("or continue with credentials"));
        panel.add(Box.createVerticalStrut(10));

        loginIdentifierField = new JTextField();
        loginIdentifierField.putClientProperty("JTextField.placeholderText", "Email or Username (e.g. admin)");
        styleField(loginIdentifierField);

        loginPasswordField = new JPasswordField();
        loginPasswordField.putClientProperty("JTextField.placeholderText", "Password (e.g. Password123)");
        styleField(loginPasswordField);

        panel.add(fieldLabel("EMAIL OR USERNAME"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(loginIdentifierField);
        panel.add(Box.createVerticalStrut(8));
        panel.add(fieldLabel("PASSWORD"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(loginPasswordField);
        panel.add(Box.createVerticalStrut(16));

        JButton signIn = createPrimaryButton("SIGN IN");
        signIn.addActionListener(e -> performLogin());

        JButton guestBtn = createGhostButton("CONTINUE AS GUEST");
        guestBtn.addActionListener(e -> performGuestLogin());

        // Demo accounts hint
        JLabel demoHint = new JLabel("Default accounts: admin / Password123 • demo / Password123");
        demoHint.setAlignmentX(Component.CENTER_ALIGNMENT);
        demoHint.setForeground(ThemeManager.TEXT_FAINT);
        demoHint.setFont(new Font("SansSerif", Font.PLAIN, 10));

        // Switch to sign-up
        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        switchPanel.setOpaque(false);
        JLabel noAccount = new JLabel("Don't have an account?");
        noAccount.setForeground(ThemeManager.TEXT_MUTED);
        noAccount.setFont(new Font("SansSerif", Font.PLAIN, 11));
        JLabel signUpLink = new JLabel("Sign Up");
        signUpLink.setForeground(ThemeManager.ACCENT_GOLD);
        signUpLink.setFont(new Font("SansSerif", Font.BOLD, 11));
        signUpLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        signUpLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { switchToSignup(); }
            @Override public void mouseEntered(MouseEvent e) { signUpLink.setForeground(ThemeManager.ACCENT_GOLD.brighter()); }
            @Override public void mouseExited(MouseEvent e)  { signUpLink.setForeground(ThemeManager.ACCENT_GOLD); }
        });
        switchPanel.add(noAccount);
        switchPanel.add(signUpLink);

        panel.add(signIn);
        panel.add(Box.createVerticalStrut(8));
        panel.add(guestBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(demoHint);
        panel.add(Box.createVerticalStrut(10));
        panel.add(switchPanel);

        getRootPane().setDefaultButton(signIn);
        return panel;
    }

    private JPanel buildSignupPanel() {
        JPanel panel = new JPanel();
        panel.setOpaque(false);
        panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));

        JLabel welcome = new JLabel("Create an account");
        welcome.setAlignmentX(Component.CENTER_ALIGNMENT);
        welcome.setForeground(Color.WHITE);
        welcome.setFont(new Font("SansSerif", Font.BOLD, 22));

        JLabel hint = new JLabel("Sign up to save your watch history & taste DNA");
        hint.setAlignmentX(Component.CENTER_ALIGNMENT);
        hint.setForeground(ThemeManager.TEXT_MUTED);
        hint.setFont(new Font("SansSerif", Font.PLAIN, 12));

        panel.add(welcome);
        panel.add(Box.createVerticalStrut(4));
        panel.add(hint);
        panel.add(Box.createVerticalStrut(12));

        // Social sign-up row
        panel.add(buildSocialRow(this::handleSocialLogin));
        panel.add(Box.createVerticalStrut(10));
        panel.add(buildDivider("or register with email"));
        panel.add(Box.createVerticalStrut(10));

        signupUsernameField = new JTextField();
        signupUsernameField.putClientProperty("JTextField.placeholderText", "Choose a Username");
        styleField(signupUsernameField);

        signupEmailField = new JTextField();
        signupEmailField.putClientProperty("JTextField.placeholderText", "Email address");
        styleField(signupEmailField);

        signupPasswordField = new JPasswordField();
        signupPasswordField.putClientProperty("JTextField.placeholderText", "Password (8+ chars)");
        styleField(signupPasswordField);

        signupConfirmPasswordField = new JPasswordField();
        signupConfirmPasswordField.putClientProperty("JTextField.placeholderText", "Confirm Password");
        styleField(signupConfirmPasswordField);

        panel.add(fieldLabel("USERNAME"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(signupUsernameField);
        panel.add(Box.createVerticalStrut(6));
        panel.add(fieldLabel("EMAIL"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(signupEmailField);
        panel.add(Box.createVerticalStrut(6));
        panel.add(fieldLabel("PASSWORD"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(signupPasswordField);
        panel.add(Box.createVerticalStrut(6));
        panel.add(fieldLabel("CONFIRM PASSWORD"));
        panel.add(Box.createVerticalStrut(4));
        panel.add(signupConfirmPasswordField);
        panel.add(Box.createVerticalStrut(14));

        JButton signUpBtn = createPrimaryButton("CREATE ACCOUNT");
        signUpBtn.addActionListener(e -> performSignup());

        // Switch to login
        JPanel switchPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 4, 0));
        switchPanel.setOpaque(false);
        JLabel haveAccount = new JLabel("Already have an account?");
        haveAccount.setForeground(ThemeManager.TEXT_MUTED);
        haveAccount.setFont(new Font("SansSerif", Font.PLAIN, 11));
        JLabel loginLink = new JLabel("Sign In");
        loginLink.setForeground(ThemeManager.ACCENT_GOLD);
        loginLink.setFont(new Font("SansSerif", Font.BOLD, 11));
        loginLink.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        loginLink.addMouseListener(new MouseAdapter() {
            @Override public void mouseClicked(MouseEvent e) { switchToLogin(); }
            @Override public void mouseEntered(MouseEvent e) { loginLink.setForeground(ThemeManager.ACCENT_GOLD.brighter()); }
            @Override public void mouseExited(MouseEvent e)  { loginLink.setForeground(ThemeManager.ACCENT_GOLD); }
        });
        switchPanel.add(haveAccount);
        switchPanel.add(loginLink);

        panel.add(signUpBtn);
        panel.add(Box.createVerticalStrut(10));
        panel.add(switchPanel);

        return panel;
    }

    /** Row of three social sign-in buttons (Google, Facebook, Instagram). */
    private static JPanel buildSocialRow(Consumer<String> onProviderSelected) {
        JPanel row = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 0));
        row.setOpaque(false);
        row.setAlignmentX(Component.CENTER_ALIGNMENT);

        row.add(createSocialButton("Google", new Color(66, 133, 244), e -> onProviderSelected.accept("GOOGLE")));
        row.add(createSocialButton("Facebook", new Color(24, 119, 242), e -> onProviderSelected.accept("FACEBOOK")));
        row.add(createSocialButton("Instagram", new Color(225, 48, 108), e -> onProviderSelected.accept("INSTAGRAM")));

        JPanel wrapper = new JPanel();
        wrapper.setOpaque(false);
        wrapper.setLayout(new BoxLayout(wrapper, BoxLayout.Y_AXIS));
        row.setAlignmentX(Component.CENTER_ALIGNMENT);
        wrapper.add(row);
        return wrapper;
    }

    private static JButton createSocialButton(String name, Color bg, java.awt.event.ActionListener action) {
        JButton btn = new JButton(name.toUpperCase());
        btn.setToolTipText("Continue with " + name);
        btn.setPreferredSize(new Dimension(104, 34));
        btn.setBackground(bg);
        btn.setForeground(Color.WHITE);
        btn.setFont(new Font("SansSerif", Font.BOLD, 10));
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        btn.addActionListener(action);

        Color darker = bg.darker();
        btn.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { btn.setBackground(darker); }
            @Override public void mouseExited(MouseEvent e)  { btn.setBackground(bg); }
        });

        return btn;
    }

    private static JPanel buildDivider(String text) {
        JPanel panel = new JPanel(new BorderLayout(8, 0));
        panel.setOpaque(false);
        panel.setAlignmentX(Component.CENTER_ALIGNMENT);
        panel.setMaximumSize(new Dimension(340, 20));

        JPanel leftLine = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.BORDER_COLOR);
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        leftLine.setOpaque(false);

        JPanel rightLine = new JPanel() {
            @Override protected void paintComponent(Graphics g) {
                g.setColor(ThemeManager.BORDER_COLOR);
                g.fillRect(0, getHeight() / 2, getWidth(), 1);
            }
        };
        rightLine.setOpaque(false);

        JLabel lbl = new JLabel(text, JLabel.CENTER);
        lbl.setForeground(ThemeManager.TEXT_MUTED);
        lbl.setFont(new Font("SansSerif", Font.PLAIN, 10));

        panel.add(leftLine, BorderLayout.WEST);
        panel.add(lbl, BorderLayout.CENTER);
        panel.add(rightLine, BorderLayout.EAST);
        return panel;
    }

    // ── Panel Switching ──────────────────────────────────────────────

    private void switchToSignup() {
        cardLayout.show(cardHost, PANEL_SIGNUP);
        card.setPreferredSize(new Dimension(410, 580));
        card.setMaximumSize(new Dimension(410, 580));
        revalidate();
        repaint();
        pack();
        setLocationRelativeTo(null);
    }

    private void switchToLogin() {
        cardLayout.show(cardHost, PANEL_LOGIN);
        card.setPreferredSize(new Dimension(410, 560));
        card.setMaximumSize(new Dimension(410, 560));
        revalidate();
        repaint();
        pack();
        setLocationRelativeTo(null);
    }

    // ── Auth Actions ─────────────────────────────────────────────────

    private void performLogin() {
        String identifier = loginIdentifierField.getText().trim();
        String password   = new String(loginPasswordField.getPassword());

        if (identifier.isEmpty() || password.isEmpty()) {
            showError("Please enter your email or username and password.");
            return;
        }

        setLoadingCursor(true);

        new SwingWorker<AuthApiClient.AuthResult, Void>() {
            @Override protected AuthApiClient.AuthResult doInBackground() throws Exception {
                return new AuthApiClient().login(identifier, password);
            }
            @Override protected void done() {
                setLoadingCursor(false);
                try {
                    AuthApiClient.AuthResult result = get();
                    applySession(result, SessionManager.AuthProvider.LOCAL, false, null);
                    completeLogin();
                } catch (Exception ex) {
                    showAuthError(ex);
                }
            }
        }.execute();
    }

    private void performSignup() {
        String username = signupUsernameField.getText().trim();
        String email    = signupEmailField.getText().trim();
        String password = new String(signupPasswordField.getPassword());
        String confirm  = new String(signupConfirmPasswordField.getPassword());

        if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
            showError("Please fill in all fields.");
            return;
        }
        if (!email.contains("@") || !email.contains(".")) {
            showError("Please enter a valid email address.");
            return;
        }
        if (password.length() < 8) {
            showError("Password must be at least 8 characters long.");
            return;
        }
        if (!password.equals(confirm)) {
            showError("Passwords do not match.");
            return;
        }

        setLoadingCursor(true);
        final String finalEmail = email.toLowerCase();

        new SwingWorker<AuthApiClient.AuthResult, Void>() {
            @Override protected AuthApiClient.AuthResult doInBackground() throws Exception {
                return new AuthApiClient().register(finalEmail, username, password);
            }
            @Override protected void done() {
                setLoadingCursor(false);
                try {
                    AuthApiClient.AuthResult result = get();
                    applySession(result, SessionManager.AuthProvider.LOCAL, false, null);
                    JOptionPane.showMessageDialog(LoginFrame.this,
                            "Account successfully created! Welcome to AfterCredits, " + result.displayName() + ".",
                            "Registration Successful",
                            JOptionPane.INFORMATION_MESSAGE);
                    completeLogin();
                } catch (Exception ex) {
                    showAuthError(ex);
                }
            }
        }.execute();
    }

    private void performGuestLogin() {
        // Automatically generate a Guest ID
        String guestId     = SessionManager.generateGuestId();
        String displayName = "Guest";

        setLoadingCursor(true);

        new SwingWorker<AuthApiClient.AuthResult, Void>() {
            @Override protected AuthApiClient.AuthResult doInBackground() throws Exception {
                return new AuthApiClient().guestLogin(guestId, displayName);
            }
            @Override protected void done() {
                setLoadingCursor(false);
                try {
                    applySession(get(), SessionManager.AuthProvider.GUEST, true, guestId);
                    completeLogin();
                } catch (Exception ex) {
                    offlineGuestLogin(guestId);
                }
            }
        }.execute();
    }

    private void handleSocialLogin(String provider) {
        setLoadingCursor(true);
        BrowserOAuthService.startBrowserAuth(
                provider,
                this,
                payload -> {
                    final SessionManager.AuthProvider authProvider = switch (payload.provider()) {
                        case "GOOGLE"    -> SessionManager.AuthProvider.GOOGLE;
                        case "FACEBOOK"  -> SessionManager.AuthProvider.FACEBOOK;
                        case "INSTAGRAM" -> SessionManager.AuthProvider.INSTAGRAM;
                        default          -> SessionManager.AuthProvider.LOCAL;
                    };

                    new SwingWorker<AuthApiClient.AuthResult, Void>() {
                        @Override protected AuthApiClient.AuthResult doInBackground() throws Exception {
                            return new AuthApiClient().oauthLogin(
                                    payload.provider(), payload.providerId(), payload.displayName(), payload.email()
                            );
                        }
                        @Override protected void done() {
                            setLoadingCursor(false);
                            try {
                                applySession(get(), authProvider, false, null);
                                completeLogin();
                            } catch (Exception ex) {
                                showAuthError(ex);
                            }
                        }
                    }.execute();
                },
                errorMessage -> {
                    setLoadingCursor(false);
                    if (errorMessage != null && !errorMessage.contains("cancelled")) {
                        showError(errorMessage);
                    }
                }
        );
    }

    // ── Session helpers ───────────────────────────────────────────────

    private void applySession(AuthApiClient.AuthResult result,
                              SessionManager.AuthProvider provider,
                              boolean isGuest,
                              String guestId) {
        String finalGuestId = isGuest ? (guestId != null ? guestId : SessionManager.generateGuestId()) : null;
        SessionManager.setSession(new SessionManager.Session(
                result.id(),
                result.displayName(),
                result.email(),
                result.token(),
                isGuest,
                finalGuestId,
                provider
        ));
    }

    private void offlineGuestLogin(String guestId) {
        SessionManager.setSession(new SessionManager.Session(
                -1L,
                "Guest",
                null,
                "offline-guest-token",
                true,
                guestId,
                SessionManager.AuthProvider.GUEST
        ));
        completeLogin();
    }

    private void showAuthError(Exception ex) {
        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
        String message = cause.getMessage();
        if (message == null || message.isBlank()) {
            message = "Authentication failed. Incorrect email/username or password.";
        }
        JOptionPane.showMessageDialog(this, message, "Authentication Failed", JOptionPane.ERROR_MESSAGE);
    }

    private void completeLogin() {
        if (onLogin != null) onLogin.run();
        dispose();
    }

    // ── UI helpers ────────────────────────────────────────────────────

    private void setLoadingCursor(boolean loading) {
        setCursor(loading
                ? Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR)
                : Cursor.getDefaultCursor());
    }

    private void showError(String message) {
        JOptionPane.showMessageDialog(this, message, "Notice", JOptionPane.WARNING_MESSAGE);
    }

    private JLabel fieldLabel(String text) {
        JLabel label = new JLabel(text);
        label.setAlignmentX(Component.CENTER_ALIGNMENT);
        label.setForeground(ThemeManager.TEXT_MUTED);
        label.setFont(new Font("SansSerif", Font.BOLD, 10));
        return label;
    }

    private void styleField(JTextField field) {
        field.setAlignmentX(Component.CENTER_ALIGNMENT);
        field.setPreferredSize(new Dimension(340, 38));
        field.setMaximumSize(new Dimension(340, 38));
        field.setBackground(ThemeManager.BG_INPUT);
        field.setForeground(Color.WHITE);
        field.setCaretColor(ThemeManager.ACCENT_GOLD);
        field.setFont(new Font("SansSerif", Font.PLAIN, 13));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR),
                new EmptyBorder(0, 12, 0, 12)
        ));
    }

    private JButton createPrimaryButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(340, 42));
        button.setMaximumSize(new Dimension(340, 42));
        button.setForeground(Color.BLACK);
        button.setBackground(ThemeManager.ACCENT_GOLD);
        button.setFont(new Font("SansSerif", Font.BOLD, 11));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createEmptyBorder());
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setBackground(ThemeManager.ACCENT_GOLD.brighter()); }
            @Override public void mouseExited(MouseEvent e)  { button.setBackground(ThemeManager.ACCENT_GOLD); }
        });

        return button;
    }

    private JButton createGhostButton(String text) {
        JButton button = new JButton(text);
        button.setAlignmentX(Component.CENTER_ALIGNMENT);
        button.setPreferredSize(new Dimension(340, 36));
        button.setMaximumSize(new Dimension(340, 36));
        button.setForeground(ThemeManager.TEXT_MUTED);
        button.setBackground(new Color(0, 0, 0, 0));
        button.setFont(new Font("SansSerif", Font.BOLD, 10));
        button.setFocusPainted(false);
        button.setBorder(BorderFactory.createLineBorder(ThemeManager.BORDER_COLOR));
        button.setContentAreaFilled(false);
        button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        button.addMouseListener(new MouseAdapter() {
            @Override public void mouseEntered(MouseEvent e) { button.setForeground(Color.WHITE); }
            @Override public void mouseExited(MouseEvent e)  { button.setForeground(ThemeManager.TEXT_MUTED); }
        });

        return button;
    }

    // ── Entrance Animation ────────────────────────────────────────────

    private void startEntranceAnimation() {
        card.setOpaque(false);
        card.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(0, 0, 0, 0)),
                new EmptyBorder(26, 32, 26, 32)
        ));

        Timer timer = new Timer(16, null);
        timer.addActionListener(e -> {
            animationProgress += 0.045f;
            float p     = Math.min(1f, animationProgress);
            float eased = 1f - (float) Math.pow(1f - p, 3);
            int   alpha = Math.min(245, (int) (245 * eased));

            card.setOpaque(true);
            card.setBackground(new Color(16, 19, 25, alpha));

            int yOffset = (int) ((1f - eased) * 24);
            card.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createLineBorder(new Color(
                            ThemeManager.BORDER_COLOR.getRed(),
                            ThemeManager.BORDER_COLOR.getGreen(),
                            ThemeManager.BORDER_COLOR.getBlue(),
                            Math.max(1, alpha)
                    )),
                    new EmptyBorder(26 + yOffset, 32, 26, 32)
            ));

            card.revalidate();
            card.repaint();
            if (p >= 1f) ((Timer) e.getSource()).stop();
        });
        timer.start();
    }

    // ── Static factory for in-app modal dialog ────────────────────────

    /**
     * Shows a complete modal sign-in/sign-up dialog from inside the running app.
     * Strictly verifies passwords and never allows entry with invalid credentials.
     *
     * @param parent    Parent component for centering
     * @param onSuccess Callback invoked on the EDT when authentication succeeds
     */
    public static void showAsDialog(Component parent, Consumer<SessionManager.Session> onSuccess) {
        JDialog dialog = new JDialog(
                parent == null ? null : SwingUtilities.getWindowAncestor(parent),
                "Authentication — AfterCredits",
                Dialog.ModalityType.APPLICATION_MODAL
        );
        dialog.setSize(440, 620);
        dialog.setLocationRelativeTo(parent);
        dialog.setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);

        JPanel root = new JPanel();
        root.setBackground(new Color(16, 19, 25));
        root.setLayout(new BoxLayout(root, BoxLayout.Y_AXIS));
        root.setBorder(new EmptyBorder(24, 30, 24, 30));

        JLabel titleLbl = new JLabel("AfterCredits Account");
        titleLbl.setForeground(Color.WHITE);
        titleLbl.setFont(new Font("SansSerif", Font.BOLD, 20));
        titleLbl.setAlignmentX(CENTER_ALIGNMENT);

        JLabel subLbl = new JLabel("Sign in, create an account, or continue as guest");
        subLbl.setForeground(new Color(140, 140, 160));
        subLbl.setFont(new Font("SansSerif", Font.PLAIN, 11));
        subLbl.setAlignmentX(CENTER_ALIGNMENT);

        // Tabs: Sign In / Create Account
        JPanel modeToggle = new JPanel(new GridLayout(1, 2, 8, 0));
        modeToggle.setOpaque(false);
        modeToggle.setMaximumSize(new Dimension(340, 34));

        JButton tabSignIn = new JButton("Sign In");
        JButton tabSignUp = new JButton("Sign Up");

        tabSignIn.setFont(new Font("SansSerif", Font.BOLD, 11));
        tabSignUp.setFont(new Font("SansSerif", Font.BOLD, 11));
        tabSignIn.setFocusPainted(false);
        tabSignUp.setFocusPainted(false);

        tabSignIn.setBackground(ThemeManager.ACCENT_GOLD);
        tabSignIn.setForeground(Color.BLACK);
        tabSignUp.setBackground(ThemeManager.BG_INPUT);
        tabSignUp.setForeground(ThemeManager.TEXT_MUTED);

        modeToggle.add(tabSignIn);
        modeToggle.add(tabSignUp);

        // Social Buttons row
        JPanel socialRow = buildSocialRow(provider -> {
            BrowserOAuthService.startBrowserAuth(
                    provider,
                    dialog,
                    payload -> {
                        SessionManager.AuthProvider p = switch (payload.provider()) {
                            case "GOOGLE"    -> SessionManager.AuthProvider.GOOGLE;
                            case "FACEBOOK"  -> SessionManager.AuthProvider.FACEBOOK;
                            case "INSTAGRAM" -> SessionManager.AuthProvider.INSTAGRAM;
                            default          -> SessionManager.AuthProvider.LOCAL;
                        };

                        try {
                            AuthApiClient.AuthResult res = new AuthApiClient().oauthLogin(
                                    payload.provider(), payload.providerId(), payload.displayName(), payload.email()
                            );
                            SessionManager.Session s = new SessionManager.Session(
                                    res.id(), res.displayName(), res.email(), res.token(), false, null, p
                            );
                            SessionManager.setSession(s);
                            dialog.dispose();
                            if (onSuccess != null) onSuccess.accept(s);
                        } catch (Exception ex) {
                            JOptionPane.showMessageDialog(dialog, "Social login failed: " + ex.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
                        }
                    },
                    errorMessage -> {
                        // User cancelled browser auth or dialog closed
                    }
            );
        });

        // Form Fields Host
        CardLayout formCards = new CardLayout();
        JPanel formHost = new JPanel(formCards);
        formHost.setOpaque(false);

        // 1. Sign In Form
        JPanel signInForm = new JPanel();
        signInForm.setLayout(new BoxLayout(signInForm, BoxLayout.Y_AXIS));
        signInForm.setOpaque(false);

        JTextField inIdentifier = new JTextField();
        inIdentifier.putClientProperty("JTextField.placeholderText", "Email or Username (e.g. admin)");
        styleDialogField(inIdentifier);

        JPasswordField inPassword = new JPasswordField();
        inPassword.putClientProperty("JTextField.placeholderText", "Password (e.g. Password123)");
        styleDialogField(inPassword);

        JButton submitSignIn = new JButton("SIGN IN");
        submitSignIn.setAlignmentX(CENTER_ALIGNMENT);
        submitSignIn.setMaximumSize(new Dimension(340, 40));
        submitSignIn.setBackground(ThemeManager.ACCENT_GOLD);
        submitSignIn.setForeground(Color.BLACK);
        submitSignIn.setFont(new Font("SansSerif", Font.BOLD, 11));
        submitSignIn.setFocusPainted(false);
        submitSignIn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        signInForm.add(Box.createVerticalStrut(8));
        signInForm.add(inIdentifier);
        signInForm.add(Box.createVerticalStrut(8));
        signInForm.add(inPassword);
        signInForm.add(Box.createVerticalStrut(14));
        signInForm.add(submitSignIn);

        // 2. Sign Up Form
        JPanel signUpForm = new JPanel();
        signUpForm.setLayout(new BoxLayout(signUpForm, BoxLayout.Y_AXIS));
        signUpForm.setOpaque(false);

        JTextField upUsername = new JTextField();
        upUsername.putClientProperty("JTextField.placeholderText", "Username");
        styleDialogField(upUsername);

        JTextField upEmail = new JTextField();
        upEmail.putClientProperty("JTextField.placeholderText", "Email");
        styleDialogField(upEmail);

        JPasswordField upPassword = new JPasswordField();
        upPassword.putClientProperty("JTextField.placeholderText", "Password (8+ chars)");
        styleDialogField(upPassword);

        JPasswordField upConfirm = new JPasswordField();
        upConfirm.putClientProperty("JTextField.placeholderText", "Confirm Password");
        styleDialogField(upConfirm);

        JButton submitSignUp = new JButton("CREATE ACCOUNT");
        submitSignUp.setAlignmentX(CENTER_ALIGNMENT);
        submitSignUp.setMaximumSize(new Dimension(340, 40));
        submitSignUp.setBackground(ThemeManager.ACCENT_GOLD);
        submitSignUp.setForeground(Color.BLACK);
        submitSignUp.setFont(new Font("SansSerif", Font.BOLD, 11));
        submitSignUp.setFocusPainted(false);
        submitSignUp.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        signUpForm.add(Box.createVerticalStrut(4));
        signUpForm.add(upUsername);
        signUpForm.add(Box.createVerticalStrut(6));
        signUpForm.add(upEmail);
        signUpForm.add(Box.createVerticalStrut(6));
        signUpForm.add(upPassword);
        signUpForm.add(Box.createVerticalStrut(6));
        signUpForm.add(upConfirm);
        signUpForm.add(Box.createVerticalStrut(12));
        signUpForm.add(submitSignUp);

        formHost.add(signInForm, "SIGN_IN");
        formHost.add(signUpForm, "SIGN_UP");

        // Tab click listeners
        tabSignIn.addActionListener(e -> {
            tabSignIn.setBackground(ThemeManager.ACCENT_GOLD);
            tabSignIn.setForeground(Color.BLACK);
            tabSignUp.setBackground(ThemeManager.BG_INPUT);
            tabSignUp.setForeground(ThemeManager.TEXT_MUTED);
            formCards.show(formHost, "SIGN_IN");
        });

        tabSignUp.addActionListener(e -> {
            tabSignUp.setBackground(ThemeManager.ACCENT_GOLD);
            tabSignUp.setForeground(Color.BLACK);
            tabSignIn.setBackground(ThemeManager.BG_INPUT);
            tabSignIn.setForeground(ThemeManager.TEXT_MUTED);
            formCards.show(formHost, "SIGN_UP");
        });

        // Submit Sign In listener: strictly validates password!
        submitSignIn.addActionListener(ev -> {
            String identifier = inIdentifier.getText().trim();
            String password   = new String(inPassword.getPassword());
            if (identifier.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please enter your email/username and password.",
                        "Missing Details", JOptionPane.WARNING_MESSAGE);
                return;
            }

            dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            submitSignIn.setEnabled(false);

            new SwingWorker<AuthApiClient.AuthResult, Void>() {
                @Override protected AuthApiClient.AuthResult doInBackground() throws Exception {
                    return new AuthApiClient().login(identifier, password);
                }
                @Override protected void done() {
                    dialog.setCursor(Cursor.getDefaultCursor());
                    submitSignIn.setEnabled(true);
                    try {
                        AuthApiClient.AuthResult r = get();
                        SessionManager.Session s = new SessionManager.Session(
                                r.id(), r.displayName(), r.email(), r.token(), false, null, SessionManager.AuthProvider.LOCAL);
                        SessionManager.setSession(s);
                        dialog.dispose();
                        if (onSuccess != null) onSuccess.accept(s);
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        String msg = cause.getMessage();
                        if (msg == null || msg.isBlank()) msg = "Incorrect email/username or password.";
                        JOptionPane.showMessageDialog(dialog, msg, "Sign In Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        // Submit Sign Up listener
        submitSignUp.addActionListener(ev -> {
            String username = upUsername.getText().trim();
            String email    = upEmail.getText().trim();
            String password = new String(upPassword.getPassword());
            String confirm  = new String(upConfirm.getPassword());

            if (username.isEmpty() || email.isEmpty() || password.isEmpty()) {
                JOptionPane.showMessageDialog(dialog, "Please fill in all fields.", "Missing Details", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!email.contains("@") || !email.contains(".")) {
                JOptionPane.showMessageDialog(dialog, "Please enter a valid email address.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (password.length() < 8) {
                JOptionPane.showMessageDialog(dialog, "Password must be at least 8 characters long.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }
            if (!password.equals(confirm)) {
                JOptionPane.showMessageDialog(dialog, "Passwords do not match.", "Validation Error", JOptionPane.WARNING_MESSAGE);
                return;
            }

            dialog.setCursor(Cursor.getPredefinedCursor(Cursor.WAIT_CURSOR));
            submitSignUp.setEnabled(false);

            new SwingWorker<AuthApiClient.AuthResult, Void>() {
                @Override protected AuthApiClient.AuthResult doInBackground() throws Exception {
                    return new AuthApiClient().register(email.toLowerCase(), username, password);
                }
                @Override protected void done() {
                    dialog.setCursor(Cursor.getDefaultCursor());
                    submitSignUp.setEnabled(true);
                    try {
                        AuthApiClient.AuthResult r = get();
                        SessionManager.Session s = new SessionManager.Session(
                                r.id(), r.displayName(), r.email(), r.token(), false, null, SessionManager.AuthProvider.LOCAL);
                        SessionManager.setSession(s);
                        JOptionPane.showMessageDialog(dialog, "Account created successfully! Welcome, " + r.displayName() + ".", "Success", JOptionPane.INFORMATION_MESSAGE);
                        dialog.dispose();
                        if (onSuccess != null) onSuccess.accept(s);
                    } catch (Exception ex) {
                        Throwable cause = ex.getCause() != null ? ex.getCause() : ex;
                        String msg = cause.getMessage();
                        if (msg == null || msg.isBlank()) msg = "Sign up failed.";
                        JOptionPane.showMessageDialog(dialog, msg, "Sign Up Failed", JOptionPane.ERROR_MESSAGE);
                    }
                }
            }.execute();
        });

        // Guest Button
        JButton guestBtn = new JButton("CONTINUE AS GUEST");
        guestBtn.setAlignmentX(CENTER_ALIGNMENT);
        guestBtn.setMaximumSize(new Dimension(340, 36));
        guestBtn.setForeground(new Color(150, 155, 175));
        guestBtn.setFont(new Font("SansSerif", Font.BOLD, 10));
        guestBtn.setFocusPainted(false);
        guestBtn.setContentAreaFilled(false);
        guestBtn.setBorder(BorderFactory.createLineBorder(new Color(60, 65, 80)));
        guestBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        guestBtn.addActionListener(ev -> {
            String guestId = SessionManager.generateGuestId();
            SessionManager.Session s = new SessionManager.Session(
                    -1L, "Guest", null, "guest-token", true, guestId, SessionManager.AuthProvider.GUEST);
            SessionManager.setSession(s);
            dialog.dispose();
            if (onSuccess != null) onSuccess.accept(s);
        });

        root.add(titleLbl);
        root.add(Box.createVerticalStrut(4));
        root.add(subLbl);
        root.add(Box.createVerticalStrut(16));
        root.add(modeToggle);
        root.add(Box.createVerticalStrut(12));
        root.add(socialRow);
        root.add(Box.createVerticalStrut(10));
        root.add(buildDivider("or enter details"));
        root.add(Box.createVerticalStrut(4));
        root.add(formHost);
        root.add(Box.createVerticalStrut(12));
        root.add(buildDivider("or"));
        root.add(Box.createVerticalStrut(10));
        root.add(guestBtn);

        dialog.setContentPane(root);
        dialog.setVisible(true);
    }

    private static void styleDialogField(JTextField field) {
        field.setAlignmentX(CENTER_ALIGNMENT);
        field.setMaximumSize(new Dimension(340, 36));
        field.setBackground(new Color(24, 28, 38));
        field.setForeground(Color.WHITE);
        field.setCaretColor(new Color(212, 175, 55));
        field.setFont(new Font("SansSerif", Font.PLAIN, 12));
        field.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(new Color(50, 58, 72)),
                new EmptyBorder(0, 10, 0, 10)
        ));
    }

    // ── Cinematic animated background ─────────────────────────────────

    private static class CinematicBackground extends JPanel {
        private float pulse = 0f;

        CinematicBackground() {
            setOpaque(true);
            Timer timer = new Timer(40, e -> { pulse += 0.018f; repaint(); });
            timer.start();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);

            Graphics2D g2 = (Graphics2D) g.create();
            int w = getWidth();
            int h = getHeight();

            GradientPaint base = new GradientPaint(
                    0, 0, new Color(5, 7, 10),
                    w, h, new Color(14, 17, 23)
            );
            g2.setPaint(base);
            g2.fillRect(0, 0, w, h);

            float x = (float) ((Math.sin(pulse) + 1) / 2.0);
            float y = (float) ((Math.cos(pulse * 0.7) + 1) / 2.0);

            RadialGradientPaint glow = new RadialGradientPaint(
                    new Point((int) (w * (0.25 + x * 0.5)), (int) (h * (0.25 + y * 0.35))),
                    Math.max(w, h) * 0.65f,
                    new float[]  {0f, 0.45f, 1f},
                    new Color[]  {
                        new Color(212, 175, 55, 28),
                        new Color(70, 75, 90, 10),
                        new Color(0, 0, 0, 0)
                    }
            );
            g2.setPaint(glow);
            g2.fillRect(0, 0, w, h);

            g2.setStroke(new BasicStroke(1f));
            for (int i = 0; i < 7; i++) {
                int yy = (int) ((h * (0.15 + i * 0.13)) + Math.sin(pulse + i) * 18);
                g2.setColor(new Color(255, 255, 255, 8));
                g2.drawLine(0, yy, w, yy - 70);
            }

            g2.dispose();
        }
    }
}
