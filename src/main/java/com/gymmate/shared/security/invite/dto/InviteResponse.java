package com.gymmate.shared.security.invite.dto;

import com.gymmate.shared.security.invite.InviteStatus;
import com.gymmate.shared.security.invite.UserInvite;
import com.gymmate.user.domain.UserRole;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for invite details.
 * Used in admin dashboard to list/manage invites.
 */
@Builder
public record InviteResponse(
    UUID id,
    UUID gymId,
    String email,
    UserRole role,
    String firstName,
    String lastName,
    InviteStatus status,
    LocalDateTime expiresAt,
    LocalDateTime acceptedAt,
    LocalDateTime createdAt,
    boolean expired
) {
    public static InviteResponse fromEntity(UserInvite invite) {
        return InviteResponse.builder()
            .id(invite.getId())
            .gymId(invite.getGymId())
            .email(invite.getEmail())
            .role(invite.getRole())
            .firstName(invite.getFirstName())
            .lastName(invite.getLastName())
            .status(invite.getStatus())
            .expiresAt(invite.getExpiresAt())
            .acceptedAt(invite.getAcceptedAt())
            .createdAt(invite.getCreatedAt())
            .expired(invite.isExpired())
            .build();
    }
}

