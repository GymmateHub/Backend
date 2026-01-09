package com.gymmate.health.api.dto;

import com.gymmate.health.domain.WorkoutExercise;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for WorkoutExercise.
 */
public record WorkoutExerciseResponse(
    UUID id,
    UUID exerciseId,
    Integer exerciseOrder,
    Integer sets,
    Integer reps,
    BigDecimal weight,
    String weightUnit,
    Integer restSeconds,
    BigDecimal distanceMeters,
    Integer durationSeconds,
    String notes
) {
    public static WorkoutExerciseResponse from(WorkoutExercise exercise) {
        return new WorkoutExerciseResponse(
            exercise.getId(),
            exercise.getExerciseId(),
            exercise.getExerciseOrder(),
            exercise.getSets(),
            exercise.getReps(),
            exercise.getWeight(),
            exercise.getWeightUnit(),
            exercise.getRestSeconds(),
            exercise.getDistanceMeters(),
            exercise.getDurationSeconds(),
            exercise.getNotes()
        );
    }
}
