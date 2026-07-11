package com.gymmate.user.api.dto;

import com.gymmate.health.domain.WorkoutLog;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A member's workout log entry, shaped for the mobile app.
 */
public record MemberWorkoutResponse(
    UUID id,
    LocalDateTime workoutDate,
    String workoutName,
    Integer durationMinutes,
    Integer totalCaloriesBurned,
    String intensityLevel,
    String notes) {

  public static MemberWorkoutResponse from(WorkoutLog workout) {
    return new MemberWorkoutResponse(
        workout.getId(),
        workout.getWorkoutDate(),
        workout.getWorkoutName(),
        workout.getDurationMinutes(),
        workout.getTotalCaloriesBurned(),
        workout.getIntensityLevel() != null ? workout.getIntensityLevel().name() : null,
        workout.getNotes());
  }
}
