package com.gymmate.user.api.dto;

import jakarta.validation.constraints.NotNull;

import java.util.UUID;

/**
 * Request DTO for a member subscribing to a membership plan.
 */
public record SubscribePlanRequest(
    @NotNull(message = "Plan ID is required") UUID planId) {
}
