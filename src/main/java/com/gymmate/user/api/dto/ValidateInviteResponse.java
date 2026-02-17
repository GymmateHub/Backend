package com.gymmate.user.api.dto;

import com.gymmate.user.domain.UserRole;
import java.time.LocalDateTime;
import java.util.UUID;

public record ValidateInviteResponse(
                String inviteToken,
                String email,
                String firstName,
                String lastName,
                UserRole role,
                String gymName,
                String invitedBy,
                UUID organisationId,
                UUID gymId,
                LocalDateTime expiresAt,
                boolean expired) {
}
