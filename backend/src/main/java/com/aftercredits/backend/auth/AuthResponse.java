package com.aftercredits.backend.auth;

public record AuthResponse(
    Long id,
    String email,
    String displayName,
    String token
) {
    public static AuthResponse from(Account account, String token) {
        return new AuthResponse(
            account.getId(),
            account.getEmail(),
            account.getDisplayName(),
            token
        );
    }
}
