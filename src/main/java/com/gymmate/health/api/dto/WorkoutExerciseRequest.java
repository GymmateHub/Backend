package com.gymmate.health.api.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for an individual exercise within a workout.
 */
public record WorkoutExerciseRequest(
    @NotNull(message = "Exercise ID is required")
    UUID exerciseId,

    Integer exerciseOrder,

    @NotNull(message = "Sets is required")
    @Min(value = 1, message = "Sets must be at least 1")
    Integer sets,

    @NotNull(message = "Reps is required")
    @Min(value = 1, message = "Reps must be at least 1")
    Integer reps,

    BigDecimal weight,

    String weightUnit,

    Integer restSeconds,

    BigDecimal distanceMeters,

    Integer durationSeconds,

    String notes
) {}
