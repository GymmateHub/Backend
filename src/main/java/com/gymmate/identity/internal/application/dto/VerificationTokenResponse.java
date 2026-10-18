package com.gymmate.identity.internal.application.dto;

import com.gymmate.shared.constants.UserRole;

import java.util.UUID;

public record VerificationTokenResponse(
    String verificationToken,
    String message,
    int expiresIn, // seconds
    String accessToken,
    String refreshToken,
    UUID userId,
    String email,
    UserRole role,
    UUID organisationId,
    UUID gymId
) {
}
