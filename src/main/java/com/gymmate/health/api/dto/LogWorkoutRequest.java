package com.gymmate.health.api.dto;

import com.gymmate.health.domain.Enums.WorkoutIntensity;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for logging a workout with exercises.
 */
public record LogWorkoutRequest(
    @NotNull(message = "Member ID is required")
    UUID memberId,

    @NotNull(message = "Workout date is required")
    @PastOrPresent(message = "Workout date cannot be in the future")
    LocalDateTime workoutDate,

    String workoutName,

    Integer durationMinutes,

    Integer totalCaloriesBurned,

    WorkoutIntensity intensityLevel,

    String notes,

    @NotEmpty(message = "Workout must contain at least one exercise")
    @Valid
    List<WorkoutExerciseRequest> exercises
) {}
