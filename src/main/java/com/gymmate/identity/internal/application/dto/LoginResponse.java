package com.gymmate.identity.internal.application.dto;

import com.gymmate.shared.constants.UserRole;

import com.gymmate.shared.security.RolePermissions;

import java.util.List;
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
        boolean emailVerified,
        // Granted by the role (shared.security.RolePermissions); the frontend shows screens from it.
        List<String> permissions
) {

    public LoginResponse(String accessToken, String refreshToken, UUID userId, String email, String firstName,
                         String lastName, UserRole role, UUID organisationId, UUID gymId, boolean emailVerified) {
        this(accessToken, refreshToken, userId, email, firstName, lastName, role, organisationId, gymId, emailVerified,
                RolePermissions.authorities(role == null ? null : role.name()));
    }
}
