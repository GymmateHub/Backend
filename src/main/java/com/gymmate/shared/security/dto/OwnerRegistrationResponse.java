package com.gymmate.shared.security.dto;

import lombok.Builder;

import java.util.UUID;

/**
 * Response DTO for owner registration.
 * Returned after successful registration, before OTP verification.
 */
@Builder
public record OwnerRegistrationResponse(
    UUID userId,
    String email,
    String firstName,
    String lastName,
    UUID organisationId,
    String organisationName,
    UUID gymId,
    String gymName,
    String message,
    int expiresIn
) {}

