package com.gymmate.health.api.dto;

import com.gymmate.health.domain.HealthMetric;
import com.gymmate.health.domain.MetricType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for HealthMetric.
 */
public record HealthMetricResponse(
    UUID id,
    UUID memberId,
    LocalDateTime measurementDate,
    MetricType metricType,
    BigDecimal value,
    String unit,
    String notes,
    UUID recordedByUserId,
    LocalDateTime createdAt
) {
    public static HealthMetricResponse from(HealthMetric metric) {
        return new HealthMetricResponse(
            metric.getId(),
            metric.getMemberId(),
            metric.getMeasurementDate(),
            metric.getMetricType(),
            metric.getValue(),
            metric.getUnit(),
            metric.getNotes(),
            metric.getRecordedByUserId(),
            metric.getCreatedAt()
        );
    }
}
