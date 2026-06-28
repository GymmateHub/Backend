package com.gymmate.access.api.dto;

import com.gymmate.access.domain.enums.AccessDirection;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * A credential scan at an access point (from a kiosk, turnstile, or mobile app).
 */
public record ScanRequest(
    @NotBlank String token,
    @NotNull UUID accessPointId,
    AccessDirection direction
) {
}
