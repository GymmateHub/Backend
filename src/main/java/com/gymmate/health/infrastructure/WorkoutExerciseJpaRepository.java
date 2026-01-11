package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.WorkoutExercise;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

/**
 * JPA repository for WorkoutExercise entity.
 * Provides data access operations using Spring Data JPA.
 */
@Repository
public interface WorkoutExerciseJpaRepository extends JpaRepository<WorkoutExercise, UUID> {

    /**
     * Find all exercises for a workout log.
     */
    @Query("SELECT we FROM WorkoutExercise we WHERE we.workoutLogId = :workoutLogId AND we.active = true")
    List<WorkoutExercise> findByWorkoutLogId(@Param("workoutLogId") UUID workoutLogId);

    /**
     * Find exercises for a workout ordered by exercise order.
     */
    @Query("SELECT we FROM WorkoutExercise we WHERE we.workoutLogId = :workoutLogId AND we.active = true ORDER BY we.exerciseOrder ASC")
    List<WorkoutExercise> findByWorkoutLogIdOrderByExerciseOrder(@Param("workoutLogId") UUID workoutLogId);

    /**
     * Find all workout exercises for a specific exercise (for exercise usage analytics).
     */
    @Query("SELECT we FROM WorkoutExercise we WHERE we.exerciseId = :exerciseId AND we.active = true ORDER BY we.createdAt DESC")
    List<WorkoutExercise> findByExerciseId(@Param("exerciseId") UUID exerciseId);

    /**
     * Count exercises in a workout.
     */
    @Query("SELECT COUNT(we) FROM WorkoutExercise we WHERE we.workoutLogId = :workoutLogId AND we.active = true")
    long countByWorkoutLogId(@Param("workoutLogId") UUID workoutLogId);

    /**
     * Soft delete all exercises for a workout log.
     */
    @Modifying
    @Query("UPDATE WorkoutExercise we SET we.active = false WHERE we.workoutLogId = :workoutLogId")
    void softDeleteByWorkoutLogId(@Param("workoutLogId") UUID workoutLogId);
}
