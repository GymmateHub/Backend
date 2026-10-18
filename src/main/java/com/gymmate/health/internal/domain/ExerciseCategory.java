package com.gymmate.health.internal.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import lombok.*;

/**
 * ExerciseCategory entity for organizing exercises.
 * Categories: Strength, Cardio, Flexibility, Plyometrics, Core, Sports, Recovery.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class ExerciseCategory extends BaseAuditEntity {

    private String name; // Strength, Cardio, Flexibility, etc.

    private String description;

    private String iconUrl;

    private Integer displayOrder;
}
