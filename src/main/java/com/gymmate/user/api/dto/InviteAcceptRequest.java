package com.gymmate.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record InviteAcceptRequest(
        @NotBlank(message = "Invite token is required") String inviteToken,

        // BUG-032: was min=8 here but AuthenticationService.validatePassword() -> PasswordPolicyService
        // actually enforces 12+ chars with upper/lower/digit/special — an 8-11 char password used to
        // pass this annotation and then fail a moment later with a different error.
        @NotBlank(message = "Password is required") @Size(min = 12, message = "Password must be at least 12 characters long") String password,

        @Size(max = 100) String firstName,

        @Size(max = 100) String lastName,

        @Size(max = 20) String phone) {
}
