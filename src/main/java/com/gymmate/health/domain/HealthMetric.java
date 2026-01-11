package com.gymmate.health.domain;

import com.gymmate.health.domain.Enums.MetricType;
import com.gymmate.shared.domain.GymScopedEntity;
import com.gymmate.shared.exception.DomainException;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * HealthMetric entity for tracking body composition and health measurements over time.
 * Supports weight, body fat %, BMI, circumferences, vital signs, and fitness metrics.
 * Implements FR-015: Body Composition Tracking.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "health_metrics", indexes = {
    @Index(name = "idx_metric_member_type_date", columnList = "member_id,metric_type,measurement_date"),
    @Index(name = "idx_metric_gym_date", columnList = "gym_id,measurement_date")
})
public class HealthMetric extends GymScopedEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "measurement_date", nullable = false)
    private LocalDateTime measurementDate;

    @Enumerated(EnumType.STRING)
    @Column(name = "metric_type", nullable = false, length = 50)
    private MetricType metricType;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal value;

    @Column(nullable = false, length = 10)
    private String unit; // kg, lbs, %, cm, bpm, etc.

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "recorded_by_user_id")
    private UUID recordedByUserId;

    // Business methods

    /**
     * Calculate BMI from weight (kg) and height (m).
     */
    public static BigDecimal calculateBMI(BigDecimal weightKg, BigDecimal heightM) {
        if (heightM == null || heightM.compareTo(BigDecimal.ZERO) == 0) {
            throw new DomainException("INVALID_HEIGHT", "Height cannot be zero or null");
        }
        if (weightKg == null || weightKg.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("INVALID_WEIGHT", "Weight must be positive");
        }
        return weightKg.divide(heightM.pow(2), 2, RoundingMode.HALF_UP);
    }

    /**
     * Check if this metric value is an outlier compared to expected value.
     */
    public boolean isOutlier(BigDecimal expectedValue, BigDecimal threshold) {
        if (expectedValue == null || threshold == null) {
            return false;
        }
        BigDecimal diff = value.subtract(expectedValue).abs();
        return diff.compareTo(threshold) > 0;
    }

    /**
     * Validate metric value is positive.
     */
    public void validate() {
        if (value == null || value.compareTo(BigDecimal.ZERO) <= 0) {
            throw new DomainException("INVALID_METRIC_VALUE", "Metric value must be positive");
        }
        if (unit == null || unit.isBlank()) {
            throw new DomainException("INVALID_UNIT", "Unit is required");
        }
    }

    /**
     * Check if this is a body composition metric.
     */
    public boolean isBodyComposition() {
        return metricType == MetricType.WEIGHT ||
               metricType == MetricType.BODY_FAT_PERCENTAGE ||
               metricType == MetricType.MUSCLE_MASS ||
               metricType == MetricType.BMI;
    }

    /**
     * Check if this is a vital sign metric.
     */
    public boolean isVitalSign() {
        return metricType == MetricType.BLOOD_PRESSURE_SYSTOLIC ||
               metricType == MetricType.BLOOD_PRESSURE_DIASTOLIC ||
               metricType == MetricType.RESTING_HEART_RATE;
    }
}
