package com.gymmate.user.api.dto;

import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRole;
import com.gymmate.user.domain.UserStatus;

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

    public static UserResponse fromEntity(User user) {
        return new UserResponse(
                user.getId(),
                user.getOrganisationId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt(),
                user.getLastLoginAt()
        );
    }

    public String getFullName() {
        return firstName + " " + lastName;
    }
}
