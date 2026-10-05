package com.gymmate.identity.api.dto;

import com.gymmate.shared.constants.UserRole;

import java.util.UUID;

/**
 * Self-service registration of a new, not yet verified user account (status INACTIVE,
 * e-mail unverified until the OTP is confirmed).
 *
 * @param organisationId optional; null for owners whose organisation is created afterwards
 */
public record NewUserRegistration(
        String email,
        String firstName,
        String lastName,
        String rawPassword,
        String phone,
        UserRole role,
        UUID organisationId) {
}
