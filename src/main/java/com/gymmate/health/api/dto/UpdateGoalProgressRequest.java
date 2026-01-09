package com.gymmate.health.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * Request DTO for updating goal progress.
 */
public record UpdateGoalProgressRequest(
    @NotNull(message = "Current value is required")
    @DecimalMin(value = "0.0", message = "Current value must be non-negative")
    BigDecimal currentValue
) {}
