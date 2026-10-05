package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
import com.gymmate.health.internal.domain.Exercise;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link Exercise} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "Exercise")
@Table(name = "exercises")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(Exercise.class)
public class ExerciseJpaEntity extends BaseAuditJpaEntity {

    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "category_id")
    private UUID categoryId;

    @Column(name = "primary_muscle_group", length = 50)
    private String // Chest, Back, Legs, Shoulders, Arms, Core, etc.
    primaryMuscleGroup;

    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "secondary_muscle_groups", columnDefinition = "text[]")
    private String[] secondaryMuscleGroups;

    @Column(name = "equipment_required", length = 100)
    private String // Barbell, Dumbbells, None, etc.
    equipmentRequired;

    @Column(name = "difficulty_level", length = 20)
    private String // BEGINNER, INTERMEDIATE, ADVANCED
    difficultyLevel;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "instructions", columnDefinition = "jsonb")
    private String // Step-by-step instructions as JSON array
    instructions;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "thumbnail_url", length = 500)
    private String thumbnailUrl;

    @Column(name = "is_public")
    private boolean // true = public library, false = gym-specific
    isPublic = true;

    @Column(name = "created_by_gym_id")
    private UUID // Null if public exercise
    createdByGymId;
}
