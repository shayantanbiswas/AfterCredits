package com.aftercredits.backend.auth;

public record CurrentAccountResponse(
    Long id,
    String email,
    String displayName
) {
    public static CurrentAccountResponse from(Account account) {
        return new CurrentAccountResponse(
            account.getId(),
            account.getEmail(),
            account.getDisplayName()
        );
    }
}
