package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.health.internal.application.port.HealthMetricRepository;
import com.gymmate.health.internal.domain.HealthMetric;
import com.gymmate.health.internal.domain.enums.MetricType;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link HealthMetricRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class HealthMetricRepositoryAdapter extends DomainRepositoryAdapter implements HealthMetricRepository {

    private final HealthMetricJpaRepository jpaRepository;

    public HealthMetricRepositoryAdapter(HealthMetricJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public HealthMetric save(HealthMetric healthMetric) {
        return save(jpaRepository, healthMetric);
    }

    @Override
    public Optional<HealthMetric> findById(UUID id) {
        return this.<Optional<HealthMetric>>fromJpa(jpaRepository.findById(id));
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
    public void delete(HealthMetric healthMetric) {
        healthMetric.setActive(false);
        save(jpaRepository, healthMetric);
    }

    @Override
    public List<HealthMetric> findByMemberIdOrderByDateDesc(UUID memberId) {
        return this.<List<HealthMetric>>fromJpa(jpaRepository.findByMemberIdOrderByDateDesc(memberId));
    }

    @Override
    public List<HealthMetric> saveAll(Iterable<HealthMetric> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<HealthMetric> findAll() {
        return this.<List<HealthMetric>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<HealthMetric> findAllById(Iterable<UUID> ids) {
        return this.<List<HealthMetric>>fromJpa(jpaRepository.findAllById(ids));
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteAll(Iterable<HealthMetric> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public HealthMetric saveAndFlush(HealthMetric entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<HealthMetric> findAll(Pageable pageable) {
        return this.<Page<HealthMetric>>fromJpa(jpaRepository.findAll(pageable));
    }
}
