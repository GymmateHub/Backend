package com.gymmate.billing.internal.application.dto;

import jakarta.validation.constraints.NotBlank;

public record ChangeTierRequest(
        @NotBlank(message = "New tier name is required")
        String newTierName
) {
}
