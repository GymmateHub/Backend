package com.gymmate.health.internal.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import lombok.*;

import java.util.UUID;

/**
 * Exercise entity representing exercises in the library.
 * Exercises can be public (available to all gyms) or gym-specific (custom exercises).
 * Implements FR-013: Exercise Library with videos and instructions.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class Exercise extends BaseAuditEntity {

    private String name;

    private String description;

    private UUID categoryId;

    private String primaryMuscleGroup; // Chest, Back, Legs, Shoulders, Arms, Core, etc.

    private 
    String[] secondaryMuscleGroups;

    private String equipmentRequired; // Barbell, Dumbbells, None, etc.

    private String difficultyLevel; // BEGINNER, INTERMEDIATE, ADVANCED

    private 
    String instructions; // Step-by-step instructions as JSON array

    private String videoUrl;

    private String thumbnailUrl;

    @Builder.Default
    private 
    boolean isPublic = true; // true = public library, false = gym-specific

    private UUID createdByGymId; // Null if public exercise
}
