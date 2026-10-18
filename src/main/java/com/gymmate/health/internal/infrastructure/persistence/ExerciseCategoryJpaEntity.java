package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import com.gymmate.health.internal.domain.ExerciseCategory;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link ExerciseCategory} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "ExerciseCategory")
@Table(name = "exercise_categories")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(ExerciseCategory.class)
public class ExerciseCategoryJpaEntity extends BaseAuditJpaEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name; // Strength, Cardio, Flexibility, etc.

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(name = "display_order")
    private Integer displayOrder;
}
