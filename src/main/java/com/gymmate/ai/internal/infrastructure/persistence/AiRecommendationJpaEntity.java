package com.gymmate.ai.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.util.UUID;
import com.gymmate.ai.internal.domain.AiRecommendation;
import lombok.Getter;
import lombok.Setter;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link AiRecommendation} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "AiRecommendation")
@Table(name = "ai_recommendations")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(AiRecommendation.class)
public class AiRecommendationJpaEntity extends GymScopedJpaEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "workout_plan", columnDefinition = "TEXT")
    private String workoutPlan;

    @Column(name = "meal_plan", columnDefinition = "TEXT")
    private String mealPlan;

    /**
     * The fitness goals that were used to generate this plan.
     */
    @JdbcTypeCode(SqlTypes.ARRAY)
    @Column(name = "goals_used", columnDefinition = "text[]")
    private String[] goalsUsed;

    @Column(name = "experience_level", length = 30)
    private String experienceLevel;
}
