package com.gymmate.health.api.dto;

import com.gymmate.health.domain.WorkoutIntensity;
import com.gymmate.health.domain.WorkoutLog;
import com.gymmate.health.domain.WorkoutStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO for WorkoutLog with exercises.
 */
public record WorkoutLogResponse(
    UUID id,
    UUID gymId,
    UUID memberId,
    LocalDateTime workoutDate,
    String workoutName,
    Integer durationMinutes,
    Integer totalCaloriesBurned,
    WorkoutIntensity intensityLevel,
    String notes,
    WorkoutStatus status,
    List<WorkoutExerciseResponse> exercises,
    LocalDateTime createdAt
) {
    public static WorkoutLogResponse from(WorkoutLog log, List<WorkoutExerciseResponse> exercises) {
        return new WorkoutLogResponse(
            log.getId(),
            log.getGymId(),
            log.getMemberId(),
            log.getWorkoutDate(),
            log.getWorkoutName(),
            log.getDurationMinutes(),
            log.getTotalCaloriesBurned(),
            log.getIntensityLevel(),
            log.getNotes(),
            log.getStatus(),
            exercises,
            log.getCreatedAt()
        );
    }
}
