package com.gymmate.identity.internal.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ResendOtpRequest(
    @NotBlank(message = "User ID is required")
    String userId
) {
}
