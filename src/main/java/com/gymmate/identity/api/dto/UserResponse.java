package com.gymmate.identity.api.dto;

import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.constants.UserStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * DTO for user responses.
 */
public record UserResponse(
    UUID id,
    UUID organisationId,
    String email,
    String firstName,
    String lastName,
    String phone,
    UserRole role,
    UserStatus status,
    boolean active,
    LocalDateTime createdAt,
    LocalDateTime updatedAt,
    LocalDateTime lastLoginAt,
    // Granted by the role (shared.security.RolePermissions); the frontend shows screens from it.
    List<String> permissions
) {


    public String getFullName() {
        return firstName + " " + lastName;
    }
}
