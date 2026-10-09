package com.gymmate.organisation.internal.application.dto;

import java.util.UUID;

/**
 * Response DTO for gym switch operation.
 * Returns new JWT tokens with the selected gym context.
 */
public record GymSwitchResponse(
        UUID gymId,
        String gymName,
        UUID organisationId,
        String accessToken,
        String refreshToken,
        String message
) {
}
