package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.SoftDeletingJpaDomainRepositoryAdapter;
import com.gymmate.health.internal.application.port.HealthMetricRepository;
import com.gymmate.health.internal.domain.HealthMetric;
import com.gymmate.health.internal.domain.enums.MetricType;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link HealthMetricRepository} with Spring Data JPA; CRUD (with soft
 * delete) comes from {@link SoftDeletingJpaDomainRepositoryAdapter}, only the HealthMetric finders live here.
 */
@Component
@Transactional()
public class HealthMetricRepositoryAdapter extends SoftDeletingJpaDomainRepositoryAdapter<HealthMetric, UUID, HealthMetricJpaRepository>
        implements HealthMetricRepository {

    public HealthMetricRepositoryAdapter(HealthMetricJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<HealthMetric> findByMemberId(UUID memberId) {
        return this.<List<HealthMetric>>fromJpa(jpaRepository.findByMemberIdOrderByDateDesc(memberId));
    }

    @Override
    public List<HealthMetric> findByMemberIdAndMetricType(UUID memberId, MetricType metricType) {
        return this.<List<HealthMetric>>fromJpa(jpaRepository.findByMemberIdAndMetricType(memberId, metricType));
    }

    @Override
    public List<HealthMetric> findByMemberIdAndMetricTypeAndDateRange(UUID memberId, MetricType metricType, LocalDateTime startDate, LocalDateTime endDate) {
        return this.<List<HealthMetric>>fromJpa(jpaRepository.findByMemberIdAndMetricTypeAndDateRange(memberId, metricType, startDate, endDate));
    }

    @Override
    public List<HealthMetric> findByMemberIdAndDateRange(UUID memberId, LocalDateTime startDate, LocalDateTime endDate) {
        return this.<List<HealthMetric>>fromJpa(jpaRepository.findByMemberIdAndDateRange(memberId, startDate, endDate));
    }

    @Override
    public Optional<HealthMetric> findLatestByMemberIdAndMetricType(UUID memberId, MetricType metricType) {
        return this.<Optional<HealthMetric>>fromJpa(jpaRepository.findLatestByMemberIdAndMetricType(memberId, metricType));
    }

    @Override
    public List<HealthMetric> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return this.<List<HealthMetric>>fromJpa(jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate));
    }

    @Override
    public long countByMemberId(UUID memberId) {
        return jpaRepository.countByMemberId(memberId);
    }

    @Override
    public List<HealthMetric> findByMemberIdOrderByDateDesc(UUID memberId) {
        return this.<List<HealthMetric>>fromJpa(jpaRepository.findByMemberIdOrderByDateDesc(memberId));
    }
}
