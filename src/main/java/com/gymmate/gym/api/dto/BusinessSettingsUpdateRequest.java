package com.gymmate.gym.api.dto;

import jakarta.validation.constraints.Positive;

/**
 * DTO for updating gym business settings as a record.
 */
public record BusinessSettingsUpdateRequest(
    String timezone,
    String currency,
    String businessHours,

    @Positive(message = "Max members must be positive")
    Integer maxMembers,

    String featuresEnabled
) {}
