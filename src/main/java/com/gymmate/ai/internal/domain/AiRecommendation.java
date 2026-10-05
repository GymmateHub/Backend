package com.gymmate.ai.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.UUID;

/**
 * Entity for storing AI-generated workout and meal plans.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class AiRecommendation extends GymScopedEntity {

    private UUID memberId;

    private String workoutPlan;

    private String mealPlan;

    /** The fitness goals that were used to generate this plan. */
    private 
    String[] goalsUsed;

    private String experienceLevel;
}
