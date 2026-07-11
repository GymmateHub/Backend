package com.gymmate.user.api.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

/**
 * Request DTO for a member recording their own health metric
 * (e.g. WEIGHT, BODY_FAT_PERCENTAGE — see MetricType).
 */
public record RecordMetricRequest(
    @NotBlank(message = "Metric type is required") String metricType,
    @NotNull(message = "Value is required")
    @DecimalMin(value = "0.0", message = "Value cannot be negative")
    BigDecimal value,
    @Size(max = 20) String unit,
    String notes) {
}
