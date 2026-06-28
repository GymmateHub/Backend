package com.gymmate.ai.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.util.UUID;

/**
 * Entity for storing AI-generated workout and meal plans.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "ai_recommendations")
public class AiRecommendation extends GymScopedEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "workout_plan", columnDefinition = "TEXT")
    private String workoutPlan;

    @Column(name = "meal_plan", columnDefinition = "TEXT")
    private String mealPlan;

    /** The fitness goals that were used to generate this plan. */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "goals_used", columnDefinition = "text[]")
    private String[] goalsUsed;

    @Column(name = "experience_level", length = 30)
    private String experienceLevel;
}
