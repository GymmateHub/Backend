package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.WorkoutLog;
import com.gymmate.health.domain.Enums.WorkoutStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing WorkoutLogRepository using JPA.
 * Bridges domain layer with infrastructure layer.
 */
@Component
@RequiredArgsConstructor
public class WorkoutLogRepositoryAdapter implements WorkoutLogRepository {

    private final WorkoutLogJpaRepository jpaRepository;

    @Override
    public WorkoutLog save(WorkoutLog workoutLog) {
        return jpaRepository.save(workoutLog);
    }

    @Override
    public Optional<WorkoutLog> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<WorkoutLog> findByMemberId(UUID memberId) {
        return jpaRepository.findByMemberIdOrderByWorkoutDateDesc(memberId);
    }

    @Override
    public List<WorkoutLog> findByMemberIdAndDateRange(UUID memberId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByMemberIdAndDateRange(memberId, startDate, endDate);
    }

    @Override
    public Page<WorkoutLog> findByMemberIdOrderByDateDesc(UUID memberId, Pageable pageable) {
        return jpaRepository.findByMemberIdPaginated(memberId, pageable);
    }

    @Override
    public List<WorkoutLog> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate);
    }

    @Override
    public List<WorkoutLog> findByMemberIdAndStatus(UUID memberId, WorkoutStatus status) {
        return jpaRepository.findByMemberIdAndStatus(memberId, status);
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
        jpaRepository.save(workoutLog);
    }

    @Override
    public Optional<WorkoutLog> findLatestByMemberId(UUID memberId) {
        return jpaRepository.findLatestByMemberId(memberId);
    }
}
