package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.SoftDeletingJpaDomainRepositoryAdapter;
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
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link WorkoutLogRepository} with Spring Data JPA; CRUD (with soft
 * delete) comes from {@link SoftDeletingJpaDomainRepositoryAdapter}, only the WorkoutLog finders live here.
 */
@Component
@Transactional()
public class WorkoutLogRepositoryAdapter extends SoftDeletingJpaDomainRepositoryAdapter<WorkoutLog, UUID, WorkoutLogJpaRepository>
        implements WorkoutLogRepository {

    public WorkoutLogRepositoryAdapter(WorkoutLogJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
