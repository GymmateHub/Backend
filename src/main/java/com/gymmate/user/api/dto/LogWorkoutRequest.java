package com.gymmate.user.api.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.time.LocalDateTime;

/**
 * Request DTO for a member logging their own workout.
 * Intensity accepts LOW / MEDIUM / HIGH / VERY_HIGH (case-insensitive).
 */
public record LogWorkoutRequest(
    LocalDateTime workoutDate,
    @Size(max = 200) String workoutName,
    @NotNull(message = "Duration is required")
    @Min(value = 1, message = "Duration must be at least 1 minute")
    @Max(value = 1440, message = "Duration cannot exceed 24 hours")
    Integer durationMinutes,
    @Min(value = 0, message = "Calories cannot be negative") Integer caloriesBurned,
    String intensity,
    String notes) {
}
