package com.gymmate.shared.security.dto;

import com.gymmate.shared.security.annotation.NoXss;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

public record LoginRequest (
    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    @NoXss(message = "Email contains invalid characters")
    String email,

    @NotBlank(message = "Password is required")
    @NoXss
    @Size(min = 8, message = "Password must be at least 8 characters long")
    String password) {}
