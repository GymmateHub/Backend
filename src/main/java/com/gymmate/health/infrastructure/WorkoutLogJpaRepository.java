package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.WorkoutLog;
import com.gymmate.health.domain.WorkoutStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for WorkoutLog entity.
 * Provides data access operations using Spring Data JPA.
 */
@Repository
public interface WorkoutLogJpaRepository extends JpaRepository<WorkoutLog, UUID> {

    /**
     * Find all workout logs for a member ordered by date descending.
     */
    @Query("SELECT w FROM WorkoutLog w WHERE w.memberId = :memberId AND w.active = true ORDER BY w.workoutDate DESC")
    List<WorkoutLog> findByMemberIdOrderByWorkoutDateDesc(@Param("memberId") UUID memberId);

    /**
     * Find workout logs within date range.
     */
    @Query("SELECT w FROM WorkoutLog w WHERE w.memberId = :memberId AND w.workoutDate BETWEEN :startDate AND :endDate AND w.active = true ORDER BY w.workoutDate DESC")
    List<WorkoutLog> findByMemberIdAndDateRange(
        @Param("memberId") UUID memberId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find paginated workout logs for a member.
     */
    @Query("SELECT w FROM WorkoutLog w WHERE w.memberId = :memberId AND w.active = true")
    Page<WorkoutLog> findByMemberIdPaginated(@Param("memberId") UUID memberId, Pageable pageable);

    /**
     * Find workout logs by gym and date range.
     */
    @Query("SELECT w FROM WorkoutLog w WHERE w.gymId = :gymId AND w.workoutDate BETWEEN :startDate AND :endDate AND w.active = true ORDER BY w.workoutDate DESC")
    List<WorkoutLog> findByGymIdAndDateRange(
        @Param("gymId") UUID gymId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find workout logs by status.
     */
    @Query("SELECT w FROM WorkoutLog w WHERE w.memberId = :memberId AND w.status = :status AND w.active = true ORDER BY w.workoutDate DESC")
    List<WorkoutLog> findByMemberIdAndStatus(
        @Param("memberId") UUID memberId,
        @Param("status") WorkoutStatus status
    );

    /**
     * Count total workouts for a member.
     */
    @Query("SELECT COUNT(w) FROM WorkoutLog w WHERE w.memberId = :memberId AND w.active = true")
    long countByMemberId(@Param("memberId") UUID memberId);

    /**
     * Count workouts within date range.
     */
    @Query("SELECT COUNT(w) FROM WorkoutLog w WHERE w.memberId = :memberId AND w.workoutDate BETWEEN :startDate AND :endDate AND w.active = true")
    long countByMemberIdAndDateRange(
        @Param("memberId") UUID memberId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find latest workout for a member.
     */
    @Query("SELECT w FROM WorkoutLog w WHERE w.memberId = :memberId AND w.active = true ORDER BY w.workoutDate DESC LIMIT 1")
    Optional<WorkoutLog> findLatestByMemberId(@Param("memberId") UUID memberId);
}
