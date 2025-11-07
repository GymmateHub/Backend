package com.gymmate.shared.security.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

import java.util.UUID;

@Data
public class RefreshTokenRequest {
    @NotBlank(message = "Refresh token is required")
    private String refreshToken;

    // Optional tenant id for multi-tenant tokens
    private UUID tenantId;
}
