package com.gymmate.shared.security.invite.dto;

import com.gymmate.user.domain.UserRole;
import lombok.Builder;

import java.util.UUID;

/**
 * Response DTO for successful invite acceptance.
 * Returns JWT tokens so user is immediately logged in.
 */
@Builder
public record InviteAcceptResponse(
    String accessToken,
    String refreshToken,
    UUID userId,
    String email,
    String firstName,
    String lastName,
    UserRole role,
    UUID organisationId,
    UUID gymId,
    boolean emailVerified
) {}

