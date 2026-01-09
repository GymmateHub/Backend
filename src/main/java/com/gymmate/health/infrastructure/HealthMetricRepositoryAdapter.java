package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.HealthMetric;
import com.gymmate.health.domain.Enums.MetricType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing HealthMetricRepository using JPA.
 * Bridges domain layer with infrastructure layer.
 */
@Component
@RequiredArgsConstructor
public class HealthMetricRepositoryAdapter implements HealthMetricRepository {

    private final HealthMetricJpaRepository jpaRepository;

    @Override
    public HealthMetric save(HealthMetric healthMetric) {
        return jpaRepository.save(healthMetric);
    }

    @Override
    public Optional<HealthMetric> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<HealthMetric> findByMemberId(UUID memberId) {
        return jpaRepository.findByMemberIdOrderByDateDesc(memberId);
    }

    @Override
    public List<HealthMetric> findByMemberIdAndMetricType(UUID memberId, MetricType metricType) {
        return jpaRepository.findByMemberIdAndMetricType(memberId, metricType);
    }

    @Override
    public List<HealthMetric> findByMemberIdAndMetricTypeAndDateRange(
            UUID memberId, MetricType metricType, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByMemberIdAndMetricTypeAndDateRange(memberId, metricType, startDate, endDate);
    }

    @Override
    public List<HealthMetric> findByMemberIdAndDateRange(UUID memberId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByMemberIdAndDateRange(memberId, startDate, endDate);
    }

    @Override
    public Optional<HealthMetric> findLatestByMemberIdAndMetricType(UUID memberId, MetricType metricType) {
        return jpaRepository.findLatestByMemberIdAndMetricType(memberId, metricType);
    }

    @Override
    public List<HealthMetric> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate);
    }

    @Override
    public long countByMemberId(UUID memberId) {
        return jpaRepository.countByMemberId(memberId);
    }

    @Override
    public void delete(HealthMetric healthMetric) {
        healthMetric.setActive(false);
        jpaRepository.save(healthMetric);
    }
}
