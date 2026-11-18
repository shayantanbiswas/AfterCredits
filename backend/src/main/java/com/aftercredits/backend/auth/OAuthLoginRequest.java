package com.aftercredits.backend.auth;

import jakarta.validation.constraints.NotBlank;

public record OAuthLoginRequest(
    @NotBlank String provider,
    @NotBlank String providerId,
    @NotBlank String displayName,
    String email,
    String accessToken
) {}
