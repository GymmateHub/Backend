package com.gymmate.identity.internal.application.dto;

import com.gymmate.shared.constants.UserRole;

import java.util.UUID;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        UUID userId,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        UUID organisationId,
        UUID gymId,
        boolean emailVerified
) {
}
