package com.gymmate.health.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.util.UUID;

/**
 * Request DTO for creating a custom gym-specific exercise.
 */
public record CreateExerciseRequest(
    @NotBlank(message = "Exercise name is required")
    String name,

    String description,

    @NotNull(message = "Category ID is required")
    UUID categoryId,

    String primaryMuscleGroup,

    String[] secondaryMuscleGroups,

    String equipmentRequired,

    @Pattern(regexp = "BEGINNER|INTERMEDIATE|ADVANCED", message = "Difficulty must be BEGINNER, INTERMEDIATE, or ADVANCED")
    String difficultyLevel,

    String instructions,

    String videoUrl
) {}
