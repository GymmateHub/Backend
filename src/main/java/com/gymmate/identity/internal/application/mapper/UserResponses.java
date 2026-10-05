package com.gymmate.identity.internal.application.mapper;

import com.gymmate.identity.api.dto.UserResponse;
import com.gymmate.identity.internal.domain.User;

/** Renders the User aggregate as the public {@link UserResponse}. */
public final class UserResponses {

    private UserResponses() {
    }

    public static UserResponse from(User user) {
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
}
