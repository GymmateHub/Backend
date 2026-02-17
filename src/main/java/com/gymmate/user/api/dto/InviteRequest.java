package com.gymmate.user.api.dto;

import com.gymmate.user.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public record InviteRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") @Size(max = 255, message = "Email must not exceed 255 characters") String email,

        @NotNull(message = "Role is required") UserRole role,

        @Size(max = 100, message = "First name must not exceed 100 characters") String firstName,

        @Size(max = 100, message = "Last name must not exceed 100 characters") String lastName) {
}
