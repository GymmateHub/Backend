package com.gymmate.health.application;

import com.gymmate.health.domain.HealthMetric;
import com.gymmate.health.domain.Enums.MetricType;
import com.gymmate.health.infrastructure.HealthMetricRepository;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service for health metrics and body composition tracking.
 * Handles recording metrics, analyzing trends, and calculating health insights.
 * Implements FR-015: Health Insights & Goals (Body Composition Tracking).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class HealthMetricService {

    private final HealthMetricRepository healthMetricRepository;

    /**
     * Record a health metric.
     */
    @Transactional
    public HealthMetric recordMetric(
            UUID organisationId,
            UUID gymId,
            UUID memberId,
            MetricType metricType,
            BigDecimal value,
            String unit,
            String notes,
            UUID recordedByUserId
    ) {
        log.info("Recording {} metric for member {}: {} {}", metricType, memberId, value, unit);

        // Create metric
        HealthMetric metric = HealthMetric.builder()
            .memberId(memberId)
            .measurementDate(LocalDateTime.now())
            .metricType(metricType)
            .value(value)
            .unit(unit)
            .notes(notes)
            .recordedByUserId(recordedByUserId)
            .build();

        // Set gym ID manually (organisationId will be set by prePersist from TenantContext)
        metric.setGymId(gymId);

        // Validate
        metric.validate();

        // Check for outliers if there's previous data
        healthMetricRepository.findLatestByMemberIdAndMetricType(memberId, metricType)
            .ifPresent(previousMetric -> {
                BigDecimal threshold = calculateThreshold(metricType, previousMetric.getValue());
                if (metric.isOutlier(previousMetric.getValue(), threshold)) {
                    log.warn("Potential outlier detected for member {}: {} {} (previous: {} {})",
                        memberId, value, unit, previousMetric.getValue(), previousMetric.getUnit());
                }
            });

        HealthMetric savedMetric = healthMetricRepository.save(metric);

        log.info("Successfully recorded metric {} for member {}", savedMetric.getId(), memberId);
        return savedMetric;
    }

    /**
     * Get latest metric for a member by type.
     */
    @Transactional(readOnly = true)
    public HealthMetric getLatestMetric(UUID memberId, MetricType metricType) {
        log.debug("Fetching latest {} for member {}", metricType, memberId);
        return healthMetricRepository.findLatestByMemberIdAndMetricType(memberId, metricType)
            .orElse(null);
    }

    /**
     * Get metric history for a member by type.
     */
    @Transactional(readOnly = true)
    public List<HealthMetric> getMetricHistory(UUID memberId, MetricType metricType) {
        log.debug("Fetching {} history for member {}", metricType, memberId);
        return healthMetricRepository.findByMemberIdAndMetricType(memberId, metricType);
    }

    /**
     * Get metric history for a member by type and date range.
     */
    @Transactional(readOnly = true)
    public List<HealthMetric> getMetricHistoryByDateRange(
            UUID memberId,
            MetricType metricType,
            LocalDate startDate,
            LocalDate endDate
    ) {
        log.debug("Fetching {} history for member {} from {} to {}", metricType, memberId, startDate, endDate);

        LocalDateTime startDateTime = startDate.atStartOfDay();
        LocalDateTime endDateTime = endDate.plusDays(1).atStartOfDay();

        return healthMetricRepository.findByMemberIdAndMetricTypeAndDateRange(
            memberId, metricType, startDateTime, endDateTime);
    }

    /**
     * Get all metrics for a member.
     */
    @Transactional(readOnly = true)
    public List<HealthMetric> getAllMetrics(UUID memberId) {
        log.debug("Fetching all metrics for member: {}", memberId);
        return healthMetricRepository.findByMemberId(memberId);
    }

    /**
     * Get latest body composition snapshot.
     */
    @Transactional(readOnly = true)
    public BodyCompositionSnapshot getLatestBodyComposition(UUID memberId) {
        log.debug("Fetching latest body composition for member: {}", memberId);

        HealthMetric weight = healthMetricRepository.findLatestByMemberIdAndMetricType(memberId, MetricType.WEIGHT)
            .orElse(null);

        HealthMetric bodyFat = healthMetricRepository.findLatestByMemberIdAndMetricType(memberId, MetricType.BODY_FAT_PERCENTAGE)
            .orElse(null);

        HealthMetric muscleMass = healthMetricRepository.findLatestByMemberIdAndMetricType(memberId, MetricType.MUSCLE_MASS)
            .orElse(null);

        HealthMetric bmi = healthMetricRepository.findLatestByMemberIdAndMetricType(memberId, MetricType.BMI)
            .orElse(null);

        HealthMetric waist = healthMetricRepository.findLatestByMemberIdAndMetricType(memberId, MetricType.WAIST_CIRCUMFERENCE)
            .orElse(null);

        return new BodyCompositionSnapshot(weight, bodyFat, muscleMass, bmi, waist);
    }

    /**
     * Analyze metric trend over time.
     */
    @Transactional(readOnly = true)
    public MetricTrend analyzeMetricTrend(UUID memberId, MetricType metricType, int days) {
        log.debug("Analyzing {} trend for member {} over {} days", metricType, memberId, days);

        LocalDate endDate = LocalDate.now();
        LocalDate startDate = endDate.minusDays(days);

        List<HealthMetric> metrics = getMetricHistoryByDateRange(memberId, metricType, startDate, endDate);

        if (metrics.size() < 2) {
            return new MetricTrend(metricType, TrendDirection.STABLE, BigDecimal.ZERO, BigDecimal.ZERO, metrics.size());
        }

        // Calculate trend
        BigDecimal firstValue = metrics.get(metrics.size() - 1).getValue();
        BigDecimal lastValue = metrics.get(0).getValue();
        BigDecimal change = lastValue.subtract(firstValue);
        BigDecimal percentageChange = firstValue.compareTo(BigDecimal.ZERO) != 0
            ? change.divide(firstValue, 4, RoundingMode.HALF_UP).multiply(BigDecimal.valueOf(100))
            : BigDecimal.ZERO;

        TrendDirection direction = determineTrendDirection(change, metricType);

        return new MetricTrend(metricType, direction, change, percentageChange, metrics.size());
    }

    /**
     * Calculate BMI from weight and height.
     */
    @Transactional
    public HealthMetric calculateAndRecordBMI(
            UUID organisationId,
            UUID gymId,
            UUID memberId,
            BigDecimal weightKg,
            BigDecimal heightM,
            UUID recordedByUserId
    ) {
        log.info("Calculating and recording BMI for member {}", memberId);

        BigDecimal bmi = HealthMetric.calculateBMI(weightKg, heightM);

        return recordMetric(organisationId, gymId, memberId, MetricType.BMI, bmi, "kg/m²", null, recordedByUserId);
    }

    /**
     * Delete a health metric.
     */
    @Transactional
    public void deleteMetric(UUID metricId) {
        log.info("Deleting health metric: {}", metricId);

        HealthMetric metric = healthMetricRepository.findById(metricId)
            .orElseThrow(() -> new ResourceNotFoundException("Health metric", metricId.toString()));

        healthMetricRepository.delete(metric);
        log.info("Successfully deleted health metric: {}", metricId);
    }

    // Helper methods

    private BigDecimal calculateThreshold(MetricType metricType, BigDecimal previousValue) {
        // Define reasonable thresholds as percentage of previous value
        return switch (metricType) {
            case WEIGHT -> previousValue.multiply(BigDecimal.valueOf(0.10)); // 10% change
            case BODY_FAT_PERCENTAGE -> BigDecimal.valueOf(5); // 5% absolute change
            case BMI -> BigDecimal.valueOf(3); // 3 points
            case MUSCLE_MASS -> previousValue.multiply(BigDecimal.valueOf(0.15)); // 15% change
            case BLOOD_PRESSURE_SYSTOLIC, BLOOD_PRESSURE_DIASTOLIC -> BigDecimal.valueOf(20); // 20 mmHg
            case RESTING_HEART_RATE -> BigDecimal.valueOf(15); // 15 bpm
            default -> previousValue.multiply(BigDecimal.valueOf(0.20)); // 20% for other metrics
        };
    }

    private TrendDirection determineTrendDirection(BigDecimal change, MetricType metricType) {
        if (change.abs().compareTo(BigDecimal.valueOf(0.01)) < 0) {
            return TrendDirection.STABLE;
        }

        return change.compareTo(BigDecimal.ZERO) > 0 ? TrendDirection.INCREASING : TrendDirection.DECREASING;
    }

    // DTOs

    public record BodyCompositionSnapshot(
        HealthMetric weight,
        HealthMetric bodyFat,
        HealthMetric muscleMass,
        HealthMetric bmi,
        HealthMetric waistCircumference
    ) {}

    public record MetricTrend(
        MetricType metricType,
        TrendDirection direction,
        BigDecimal change,
        BigDecimal percentageChange,
        int dataPoints
    ) {}

    public enum TrendDirection {
        INCREASING,
        DECREASING,
        STABLE
    }
}
