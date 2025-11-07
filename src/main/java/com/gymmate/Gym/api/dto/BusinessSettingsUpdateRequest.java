package com.gymmate.Gym.api.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating gym business settings.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BusinessSettingsUpdateRequest {

    private String timezone;
    private String currency;
    private String businessHours; // JSON string

    @Positive(message = "Max members must be positive")
    private Integer maxMembers;

    private String featuresEnabled; // JSON array string
}

