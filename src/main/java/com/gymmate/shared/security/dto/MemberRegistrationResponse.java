package com.gymmate.shared.security.dto;

import lombok.Builder;

import java.util.UUID;

/**
 * Response DTO for member registration.
 * Returned after successful registration, before OTP verification.
 */
@Builder
public record MemberRegistrationResponse(
    UUID userId,
    String email,
    String firstName,
    String lastName,
    UUID gymId,
    String gymName,
    String message,
    int expiresIn
) {}

