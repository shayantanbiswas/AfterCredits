package com.aftercredits.client.auth;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

public class AuthApiClient {
    private static final String BASE_URL =
            System.getProperty("aftercredits.api.url", "http://localhost:8080");

    private static final HttpClient CLIENT = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

    private static final Gson GSON = new Gson();

    public AuthResult login(String identifier, String password) throws Exception {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("email", identifier);
            body.addProperty("password", password);
            return post("/api/auth/login", body);
        } catch (AuthApiException apiEx) {
            // Backend rejected credentials (e.g. 401 Unauthorized) — propagate rejection!
            throw apiEx;
        } catch (Exception e) {
            // Backend server is offline / unreachable — verify against LocalAuthService
            return LocalAuthService.getInstance().login(identifier, password);
        }
    }

    public AuthResult register(String email, String displayName, String password) throws Exception {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("email", email);
            body.addProperty("displayName", displayName);
            body.addProperty("password", password);
            AuthResult res = post("/api/auth/register", body);
            try {
                LocalAuthService.getInstance().register(displayName, email, password);
            } catch (Exception ignored) {}
            return res;
        } catch (AuthApiException apiEx) {
            // Backend returned duplicate email or validation error
            throw apiEx;
        } catch (Exception e) {
            // Backend offline — register in LocalAuthService
            return LocalAuthService.getInstance().register(displayName, email, password);
        }
    }

    public AuthResult oauthLogin(String provider, String providerId, String displayName, String email) throws Exception {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("provider", provider);
            body.addProperty("providerId", providerId);
            body.addProperty("displayName", displayName);
            if (email != null) body.addProperty("email", email);
            return post("/api/auth/oauth", body);
        } catch (Exception e) {
            return LocalAuthService.getInstance().oauthLogin(provider, providerId, displayName, email);
        }
    }

    public AuthResult guestLogin(String guestId, String displayName) throws Exception {
        try {
            JsonObject body = new JsonObject();
            body.addProperty("guestId", guestId);
            body.addProperty("displayName", displayName);
            return post("/api/auth/guest", body);
        } catch (Exception e) {
            return LocalAuthService.getInstance().guestLogin(guestId, displayName);
        }
    }

    private AuthResult post(String path, JsonObject body) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + path))
                .timeout(Duration.ofSeconds(4))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        HttpResponse<String> response = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200 && response.statusCode() != 201) {
            String message = "Authentication failed. Incorrect email/username or password.";
            try {
                JsonObject error = GSON.fromJson(response.body(), JsonObject.class);
                if (error != null && error.has("error")) {
                    message = error.get("error").getAsString();
                }
            } catch (Exception ignored) {}
            throw new AuthApiException(response.statusCode(), message);
        }

        JsonObject result = GSON.fromJson(response.body(), JsonObject.class);
        return new AuthResult(
                result.get("id").getAsLong(),
                result.has("email") && !result.get("email").isJsonNull()
                        ? result.get("email").getAsString() : null,
                result.get("displayName").getAsString(),
                result.get("token").getAsString()
        );
    }

    public record AuthResult(
            long id,
            String email,
            String displayName,
            String token
    ) {}

    public static class AuthApiException extends Exception {
        private final int statusCode;

        public AuthApiException(int statusCode, String message) {
            super(message);
            this.statusCode = statusCode;
        }

        public int getStatusCode() { return statusCode; }
    }
}
