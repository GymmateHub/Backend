package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.WorkoutLog;
import com.gymmate.health.domain.Enums.WorkoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for WorkoutLog.
 * Defines domain-level operations for managing workout logs.
 */
public interface WorkoutLogRepository {

    /**
     * Save or update a workout log.
     */
    WorkoutLog save(WorkoutLog workoutLog);

    /**
     * Find workout log by ID.
     */
    Optional<WorkoutLog> findById(UUID id);

    /**
     * Find all workout logs for a member.
     */
    List<WorkoutLog> findByMemberId(UUID memberId);

    /**
     * Find workout logs for a member within date range.
     */
    List<WorkoutLog> findByMemberIdAndDateRange(UUID memberId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find recent workout logs for a member (paginated).
     */
    Page<WorkoutLog> findByMemberIdOrderByDateDesc(UUID memberId, Pageable pageable);

    /**
     * Find workout logs by gym within date range.
     */
    List<WorkoutLog> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find workout logs by status.
     */
    List<WorkoutLog> findByMemberIdAndStatus(UUID memberId, WorkoutStatus status);

    /**
     * Count total workouts for a member.
     */
    long countByMemberId(UUID memberId);

    /**
     * Count workouts for a member within date range.
     */
    long countByMemberIdAndDateRange(UUID memberId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Delete a workout log (soft delete).
     */
    void delete(WorkoutLog workoutLog);

    /**
     * Find latest workout for a member.
     */
    Optional<WorkoutLog> findLatestByMemberId(UUID memberId);
}
