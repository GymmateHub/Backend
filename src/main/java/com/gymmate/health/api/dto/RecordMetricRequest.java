package com.gymmate.health.api.dto;

import com.gymmate.health.domain.Enums.MetricType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for recording a health metric.
 */
public record RecordMetricRequest(
    @NotNull(message = "Member ID is required")
    UUID memberId,

    @NotNull(message = "Metric type is required")
    MetricType metricType,

    @NotNull(message = "Value is required")
    @DecimalMin(value = "0.01", message = "Value must be positive")
    BigDecimal value,

    @NotBlank(message = "Unit is required")
    String unit,

    String notes
) {}
