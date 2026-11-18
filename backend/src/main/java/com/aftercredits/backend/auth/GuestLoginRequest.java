package com.aftercredits.backend.auth;

import jakarta.validation.constraints.NotBlank;

public record GuestLoginRequest(
    @NotBlank String guestId,
    @NotBlank String displayName
) {}
