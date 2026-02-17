package com.gymmate.shared.security.invite.dto;

import com.gymmate.user.domain.UserRole;
import lombok.Builder;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for invite validation.
 * Returned when user clicks invite link - provides context for the accept form.
 */
@Builder
public record InviteValidateResponse(
    String inviteToken,
    String email,
    String firstName,
    String lastName,
    UserRole role,
    String gymName,
    String gymLogoUrl,
    String invitedByName,
    UUID organisationId,
    UUID gymId,
    LocalDateTime expiresAt,
    boolean expired
) {}

