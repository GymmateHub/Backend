package com.gymmate.health.api.dto;

import com.gymmate.health.domain.Exercise;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for Exercise.
 */
public record ExerciseResponse(
    UUID id,
    String name,
    String description,
    UUID categoryId,
    String primaryMuscleGroup,
    String[] secondaryMuscleGroups,
    String equipmentRequired,
    String difficultyLevel,
    String instructions,
    String videoUrl,
    String thumbnailUrl,
    boolean isPublic,
    UUID createdByGymId,
    LocalDateTime createdAt
) {
    public static ExerciseResponse from(Exercise exercise) {
        return new ExerciseResponse(
            exercise.getId(),
            exercise.getName(),
            exercise.getDescription(),
            exercise.getCategoryId(),
            exercise.getPrimaryMuscleGroup(),
            exercise.getSecondaryMuscleGroups(),
            exercise.getEquipmentRequired(),
            exercise.getDifficultyLevel(),
            exercise.getInstructions(),
            exercise.getVideoUrl(),
            exercise.getThumbnailUrl(),
            exercise.isPublic(),
            exercise.getCreatedByGymId(),
            exercise.getCreatedAt()
        );
    }
}
