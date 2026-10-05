package com.gymmate.identity.api.dto;

import com.gymmate.identity.internal.domain.UserInvite;
import com.gymmate.shared.constants.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record InviteResponse(
        UUID id,
        String email,
        UserRole role,
        String firstName,
        String lastName,
        String status,
        LocalDateTime expiresAt,
        LocalDateTime createdAt) {
    public static InviteResponse fromEntity(UserInvite invite) {
        return new InviteResponse(
                invite.getId(),
                invite.getEmail(),
                invite.getRole(),
                invite.getFirstName(),
                invite.getLastName(),
                invite.getStatus().name(),
                invite.getExpiresAt(),
                invite.getCreatedAt());
    }
}
