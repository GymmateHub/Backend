package com.gymmate.identity.internal.application.dto;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record RefreshTokenRequest(
        @NotBlank(message = "Refresh token is required")
        String refreshToken,
        // Optional tenant id for multi-tenant tokens
        UUID tenantId
) {
}
