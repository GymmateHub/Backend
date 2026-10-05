package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.health.internal.application.port.WorkoutLogRepository;
import com.gymmate.health.internal.domain.WorkoutLog;
import com.gymmate.health.internal.domain.enums.WorkoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link WorkoutLogRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class WorkoutLogRepositoryAdapter extends DomainRepositoryAdapter implements WorkoutLogRepository {

    private final WorkoutLogJpaRepository jpaRepository;

    public WorkoutLogRepositoryAdapter(WorkoutLogJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public WorkoutLog save(WorkoutLog workoutLog) {
        return save(jpaRepository, workoutLog);
    }

    @Override
    public Optional<WorkoutLog> findById(UUID id) {
        return this.<Optional<WorkoutLog>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public List<WorkoutLog> findByMemberId(UUID memberId) {
        return this.<List<WorkoutLog>>fromJpa(jpaRepository.findByMemberIdOrderByWorkoutDateDesc(memberId));
    }

    @Override
    public List<WorkoutLog> findByMemberIdAndDateRange(UUID memberId, LocalDateTime startDate, LocalDateTime endDate) {
        return this.<List<WorkoutLog>>fromJpa(jpaRepository.findByMemberIdAndDateRange(memberId, startDate, endDate));
    }

    @Override
    public Page<WorkoutLog> findByMemberIdOrderByDateDesc(UUID memberId, Pageable pageable) {
        return this.<Page<WorkoutLog>>fromJpa(jpaRepository.findByMemberIdPaginated(memberId, pageable));
    }

    @Override
    public List<WorkoutLog> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return this.<List<WorkoutLog>>fromJpa(jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate));
    }

    @Override
    public List<WorkoutLog> findByMemberIdAndStatus(UUID memberId, WorkoutStatus status) {
        return this.<List<WorkoutLog>>fromJpa(jpaRepository.findByMemberIdAndStatus(memberId, status));
    }

    @Override
    public long countByMemberId(UUID memberId) {
        return jpaRepository.countByMemberId(memberId);
    }

    @Override
    public long countByMemberIdAndDateRange(UUID memberId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.countByMemberIdAndDateRange(memberId, startDate, endDate);
    }

    @Override
    public void delete(WorkoutLog workoutLog) {
        workoutLog.setActive(false);
        save(jpaRepository, workoutLog);
    }

    @Override
    public Optional<WorkoutLog> findLatestByMemberId(UUID memberId) {
        return this.<Optional<WorkoutLog>>fromJpa(jpaRepository.findLatestByMemberId(memberId));
    }

    @Override
    public List<WorkoutLog> findByMemberIdOrderByWorkoutDateDesc(UUID memberId) {
        return this.<List<WorkoutLog>>fromJpa(jpaRepository.findByMemberIdOrderByWorkoutDateDesc(memberId));
    }

    @Override
    public Page<WorkoutLog> findByMemberIdPaginated(UUID memberId, Pageable pageable) {
        return this.<Page<WorkoutLog>>fromJpa(jpaRepository.findByMemberIdPaginated(memberId, pageable));
    }

    @Override
    public List<WorkoutLog> saveAll(Iterable<WorkoutLog> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<WorkoutLog> findAll() {
        return this.<List<WorkoutLog>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<WorkoutLog> findAllById(Iterable<UUID> ids) {
        return this.<List<WorkoutLog>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<WorkoutLog> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public WorkoutLog saveAndFlush(WorkoutLog entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<WorkoutLog> findAll(Pageable pageable) {
        return this.<Page<WorkoutLog>>fromJpa(jpaRepository.findAll(pageable));
    }
}
