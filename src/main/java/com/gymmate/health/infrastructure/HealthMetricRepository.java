package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.HealthMetric;
import com.gymmate.health.domain.Enums.MetricType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for HealthMetric.
 * Defines domain-level operations for managing health metrics.
 */
public interface HealthMetricRepository {

    /**
     * Save or update a health metric.
     */
    HealthMetric save(HealthMetric healthMetric);

    /**
     * Find health metric by ID.
     */
    Optional<HealthMetric> findById(UUID id);

    /**
     * Find all metrics for a member.
     */
    List<HealthMetric> findByMemberId(UUID memberId);

    /**
     * Find metrics by member and type.
     */
    List<HealthMetric> findByMemberIdAndMetricType(UUID memberId, MetricType metricType);

    /**
     * Find metrics by member, type, and date range.
     */
    List<HealthMetric> findByMemberIdAndMetricTypeAndDateRange(
        UUID memberId,
        MetricType metricType,
        LocalDateTime startDate,
        LocalDateTime endDate
    );

    /**
     * Find metrics by member within date range.
     */
    List<HealthMetric> findByMemberIdAndDateRange(UUID memberId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find latest metric for a member by type.
     */
    Optional<HealthMetric> findLatestByMemberIdAndMetricType(UUID memberId, MetricType metricType);

    /**
     * Find all metrics by gym and date range (for gym-wide analytics).
     */
    List<HealthMetric> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Count metrics for a member.
     */
    long countByMemberId(UUID memberId);

    /**
     * Delete a health metric (soft delete).
     */
    void delete(HealthMetric healthMetric);
}

