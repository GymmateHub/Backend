package com.gymmate.identity.api.dto;

import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.constants.UserStatus;

import java.util.UUID;

/** Read model of a platform user, exposed to other modules instead of the User aggregate. */
public record UserSummary(
        UUID id,
        String email,
        String firstName,
        String lastName,
        UserRole role,
        UserStatus status,
        UUID organisationId,
        boolean active,
        boolean emailVerified) {

    public String fullName() {
        return ((firstName == null ? "" : firstName) + " " + (lastName == null ? "" : lastName)).trim();
    }
}
