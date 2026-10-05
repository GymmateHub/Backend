package com.gymmate.scheduling.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import com.gymmate.scheduling.internal.domain.ClassCategory;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link ClassCategory} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "ClassCategory")
@Table(name = "class_categories")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(ClassCategory.class)
public class ClassCategoryJpaEntity extends GymScopedJpaEntity {

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
    @Column(nullable = false, length = 100)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 7)
    private String // Hex color for UI
    color;

    @Column(length = 50)
    private String icon;
}
