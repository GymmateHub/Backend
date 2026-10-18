package com.gymmate.health.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.health.internal.domain.HealthMetric;
import com.gymmate.health.internal.domain.enums.MetricType;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for HealthMetric.
 * Defines domain-level operations for managing health metrics.
 */
public interface HealthMetricRepository extends DomainRepository<HealthMetric, UUID> {

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

    List<HealthMetric> findByMemberIdOrderByDateDesc(UUID memberId);
}

