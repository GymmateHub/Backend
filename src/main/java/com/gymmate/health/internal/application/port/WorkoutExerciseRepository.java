package com.gymmate.health.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.health.internal.domain.WorkoutExercise;

import java.util.List;
import java.util.UUID;

/**
 * Domain repository interface for WorkoutExercise.
 * Defines domain-level operations for managing workout exercises.
 */
public interface WorkoutExerciseRepository extends DomainRepository<WorkoutExercise, UUID> {

    /**
     * Find all exercises for a workout log.
     */
    List<WorkoutExercise> findByWorkoutLogId(UUID workoutLogId);

    /**
     * Find exercises for a workout log ordered by exercise order.
     */
    List<WorkoutExercise> findByWorkoutLogIdOrderByExerciseOrder(UUID workoutLogId);

    /**
     * Find all workout exercises for a specific exercise (for analytics).
     */
    List<WorkoutExercise> findByExerciseId(UUID exerciseId);

    /**
     * Count exercises in a workout.
     */
    long countByWorkoutLogId(UUID workoutLogId);

    /**
     * Delete a workout exercise (soft delete).
     */
    void delete(WorkoutExercise workoutExercise);

    /**
     * Delete all exercises for a workout log (used when deleting workout).
     */
    void deleteByWorkoutLogId(UUID workoutLogId);

    void softDeleteByWorkoutLogId(UUID workoutLogId);
}
