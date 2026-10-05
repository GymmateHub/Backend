package com.gymmate.identity.api.dto;

import com.gymmate.shared.constants.UserRole;
import com.gymmate.shared.constants.UserStatus;

import java.time.LocalDateTime;
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
    LocalDateTime lastLoginAt
) {


    public String getFullName() {
        return firstName + " " + lastName;
    }
}
