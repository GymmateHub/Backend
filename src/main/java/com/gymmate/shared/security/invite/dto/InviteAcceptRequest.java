package com.gymmate.shared.security.invite.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

/**
 * Request DTO for accepting an invite.
 * Invitee provides password and optional name fields.
 */
public record InviteAcceptRequest(
    @NotBlank(message = "Invite token is required")
    String inviteToken,

    @NotBlank(message = "Password is required")
    @Size(min = 8, max = 100, message = "Password must be between 8 and 100 characters")
    String password,

    @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
    String firstName,

    @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
    String lastName,

    @Size(max = 20, message = "Phone number must not exceed 20 characters")
    String phone
) {}

