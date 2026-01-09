package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.HealthMetric;
import com.gymmate.health.domain.MetricType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for HealthMetric entity.
 * Provides data access operations using Spring Data JPA.
 */
@Repository
public interface HealthMetricJpaRepository extends JpaRepository<HealthMetric, UUID> {

    /**
     * Find all metrics for a member ordered by date descending.
     */
    @Query("SELECT hm FROM HealthMetric hm WHERE hm.memberId = :memberId AND hm.active = true ORDER BY hm.measurementDate DESC")
    List<HealthMetric> findByMemberIdOrderByDateDesc(@Param("memberId") UUID memberId);

    /**
     * Find metrics by member and type.
     */
    @Query("SELECT hm FROM HealthMetric hm WHERE hm.memberId = :memberId AND hm.metricType = :metricType AND hm.active = true ORDER BY hm.measurementDate DESC")
    List<HealthMetric> findByMemberIdAndMetricType(
        @Param("memberId") UUID memberId,
        @Param("metricType") MetricType metricType
    );

    /**
     * Find metrics by member, type, and date range.
     */
    @Query("SELECT hm FROM HealthMetric hm WHERE hm.memberId = :memberId AND hm.metricType = :metricType AND hm.measurementDate BETWEEN :startDate AND :endDate AND hm.active = true ORDER BY hm.measurementDate DESC")
    List<HealthMetric> findByMemberIdAndMetricTypeAndDateRange(
        @Param("memberId") UUID memberId,
        @Param("metricType") MetricType metricType,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find metrics by member within date range.
     */
    @Query("SELECT hm FROM HealthMetric hm WHERE hm.memberId = :memberId AND hm.measurementDate BETWEEN :startDate AND :endDate AND hm.active = true ORDER BY hm.measurementDate DESC")
    List<HealthMetric> findByMemberIdAndDateRange(
        @Param("memberId") UUID memberId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find latest metric for a member by type.
     */
    @Query("SELECT hm FROM HealthMetric hm WHERE hm.memberId = :memberId AND hm.metricType = :metricType AND hm.active = true ORDER BY hm.measurementDate DESC LIMIT 1")
    Optional<HealthMetric> findLatestByMemberIdAndMetricType(
        @Param("memberId") UUID memberId,
        @Param("metricType") MetricType metricType
    );

    /**
     * Find metrics by gym and date range.
     */
    @Query("SELECT hm FROM HealthMetric hm WHERE hm.gymId = :gymId AND hm.measurementDate BETWEEN :startDate AND :endDate AND hm.active = true ORDER BY hm.measurementDate DESC")
    List<HealthMetric> findByGymIdAndDateRange(
        @Param("gymId") UUID gymId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Count metrics for a member.
     */
    @Query("SELECT COUNT(hm) FROM HealthMetric hm WHERE hm.memberId = :memberId AND hm.active = true")
    long countByMemberId(@Param("memberId") UUID memberId);
}
