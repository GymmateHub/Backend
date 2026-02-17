package com.gymmate.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InviteAcceptRequest(
        @NotBlank(message = "Invite token is required") String inviteToken,

        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters long") String password,

        @Size(max = 100) String firstName,

        @Size(max = 100) String lastName,

        @Size(max = 20) String phone) {
}
