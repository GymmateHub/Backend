package com.gymmate.user.api.dto;

import com.gymmate.user.domain.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

/**
 * Unified DTO for user registration requests.
 * Handles all user roles: OWNER, ADMIN, MEMBER, TRAINER, STAFF.
 */
public record UnifiedRegistrationRequest(
        @NotBlank(message = "Email is required") @Email(message = "Email must be valid") @Size(max = 255, message = "Email must not exceed 255 characters") String email,

        @NotBlank(message = "First name is required") @Size(max = 100, message = "First name must not exceed 100 characters") String firstName,

        @NotBlank(message = "Last name is required") @Size(max = 100, message = "Last name must not exceed 100 characters") String lastName,

        @NotBlank(message = "Password is required") @Size(min = 8, message = "Password must be at least 8 characters long") String password,

        @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "Phone number must be valid") @Size(max = 20, message = "Phone number must not exceed 20 characters") String phone,

        @NotNull(message = "User role is required") UserRole role) {
}
