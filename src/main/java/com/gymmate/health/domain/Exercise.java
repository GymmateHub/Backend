package com.gymmate.health.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Exercise entity representing exercises in the library.
 * Exercises can be public (available to all gyms) or gym-specific (custom exercises).
 * Implements FR-013: Exercise Library with videos and instructions.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "exercises")
public class Exercise extends BaseAuditEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "primary_muscle_group", length = 50)
    private String primaryMuscleGroup; // Chest, Back, Legs, Shoulders, Arms, Core, etc.

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "secondary_muscle_groups", columnDefinition = "text[]")
    private String[] secondaryMuscleGroups;

    @Column(name = "equipment_required", length = 100)
    private String equipmentRequired; // Barbell, Dumbbells, None, etc.

    @Column(name = "difficulty_level", length = 20)
    private String difficultyLevel; // BEGINNER, INTERMEDIATE, ADVANCED

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "instructions", columnDefinition = "jsonb")
    private String instructions; // Step-by-step instructions as JSON array

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "is_public")
    @Builder.Default
    private boolean isPublic = true; // true = public library, false = gym-specific

    @Column(name = "created_by_gym_id")
    private UUID createdByGymId; // Null if public exercise
}
