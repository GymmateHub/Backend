package com.gymmate.health.api.dto;

import com.gymmate.health.domain.ExerciseCategory;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for ExerciseCategory.
 */
public record ExerciseCategoryResponse(
    UUID id,
    String name,
    String description,
    String iconUrl,
    Integer displayOrder,
    LocalDateTime createdAt
) {
    public static ExerciseCategoryResponse from(ExerciseCategory category) {
        return new ExerciseCategoryResponse(
            category.getId(),
            category.getName(),
            category.getDescription(),
            category.getIconUrl(),
            category.getDisplayOrder(),
            category.getCreatedAt()
        );
    }
}
