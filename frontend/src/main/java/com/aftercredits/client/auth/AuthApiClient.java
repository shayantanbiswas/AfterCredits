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
            .connectTimeout(Duration.ofSeconds(5))
            .build();

    private static final Gson GSON = new Gson();

    public AuthResult login(String email, String password)
            throws Exception {

        JsonObject body = new JsonObject();
        body.addProperty("email", email);
        body.addProperty("password", password);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(BASE_URL + "/api/auth/login"))
                .timeout(Duration.ofSeconds(10))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(GSON.toJson(body)))
                .build();

        HttpResponse<String> response = CLIENT.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );

        if (response.statusCode() != 200) {
            String message = "Login failed. Check your email and password.";

            try {
                JsonObject error = GSON.fromJson(response.body(), JsonObject.class);
                if (error != null && error.has("error")) {
                    message = error.get("error").getAsString();
                }
            } catch (Exception ignored) {
                // Keep the default message if the response isn't JSON.
            }

            throw new AuthApiException(response.statusCode(), message);
        }

        JsonObject result = GSON.fromJson(response.body(), JsonObject.class);

        return new AuthResult(
                result.get("id").getAsLong(),
                result.get("email").getAsString(),
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

        public int getStatusCode() {
            return statusCode;
        }
    }
}
