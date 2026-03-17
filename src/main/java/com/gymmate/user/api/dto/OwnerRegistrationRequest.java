package com.gymmate.user.api.dto;

import com.gymmate.shared.security.annotation.NoXss;
import com.gymmate.shared.security.annotation.SafeHtml;
import com.gymmate.shared.constants.UserRole;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record OwnerRegistrationRequest(
        @NotBlank(message = "Email is required")
        @Email(message = "Email must be valid")
        @Size(max = 255, message = "Email must not exceed 255 characters")
        @SafeHtml(message = "Email contains unsafe HTML content")
        @NoXss
        String email,

        @NotBlank(message = "First name is required")
        @Size(min = 2, max = 100, message = "First name must be between 2 and 100 characters")
        @SafeHtml
        @NoXss
        String firstName,

        @NotBlank(message = "Last name is required")
        @Size(min = 2, max = 100, message = "Last name must be between 2 and 100 characters")
        @SafeHtml
        @NoXss
        String lastName,

        @NotBlank(message = "Password is required")
        @Size(min = 8, max = 100, message = "Password must be at least 8 characters long")
        @NoXss
        String password,

        @Pattern(regexp = "^[+]?[0-9]{10,20}$", message = "Phone number must be valid")
        @SafeHtml
        @Size(max = 20, message = "Phone number must not exceed 20 characters")
        String phone,

        @NotBlank(message = "Organisation name is required")
        @SafeHtml
        @Size(min = 2, max = 100, message = "Organisation name must be between 2 and 100 characters")
        String organisationName,

        @NotBlank(message = "Gym name is required")
        @SafeHtml
        @Size(min = 2, max = 100, message = "Gym name must be between 2 and 100 characters")
        String gymName,

        @NotBlank(message = "Timezone is required")
        @SafeHtml
        String timezone,

        @SafeHtml
        @NotBlank(message = "Country is required")
        String country) {
    public UserRole role() {
        return UserRole.GYM_OWNER;
    }
}
