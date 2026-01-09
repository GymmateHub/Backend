package com.gymmate.health.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * ExerciseCategory entity for organizing exercises.
 * Categories: Strength, Cardio, Flexibility, Plyometrics, Core, Sports, Recovery.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "exercise_categories")
public class ExerciseCategory extends BaseAuditEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String name; // Strength, Cardio, Flexibility, etc.

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "icon_url", length = 255)
    private String iconUrl;

    @Column(name = "display_order")
    private Integer displayOrder;
}
