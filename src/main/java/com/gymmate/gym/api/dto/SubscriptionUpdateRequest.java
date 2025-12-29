package com.gymmate.gym.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;

/**
 * DTO for updating gym subscription.
 */
public record SubscriptionUpdateRequest(
    @NotBlank(message = "Subscription plan is required")
    String plan,

    @NotNull(message = "Expiration date is required")
    LocalDateTime expiresAt
) {}
