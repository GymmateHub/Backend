package com.gymmate.ai.api.dto;

import com.gymmate.ai.domain.AiRecommendation;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

/**
 * Response DTO carrying a member's AI-generated workout and meal plan.
 */
@Schema(description = "AI-generated personalised fitness plan for a member")
public record AiPlanResponse(

    @Schema(description = "Unique ID of this recommendation record")
    UUID recommendationId,

    @Schema(description = "Member ID this plan was generated for")
    UUID memberId,

    @Schema(description = "AI-generated weekly workout plan")
    String workoutPlan,

    @Schema(description = "AI-generated meal plan tailored to local cuisine and goals")
    String mealPlan,

    @Schema(description = "Fitness goals used to generate this plan")
    List<String> goalsUsed,

    @Schema(description = "Experience level used for this plan", example = "intermediate")
    String experienceLevel,

    @Schema(description = "Timestamp when this plan was generated")
    LocalDateTime generatedAt,

    @Schema(description = "True when the plan was served from cache (no new AI call was made)")
    boolean cached
) {

    /** Build a response from a persisted entity, flagging its cache origin. */
    public static AiPlanResponse fromEntity(AiRecommendation entity, boolean cached) {
        return new AiPlanResponse(
            entity.getId(),
            entity.getMemberId(),
            entity.getWorkoutPlan(),
            entity.getMealPlan(),
            entity.getGoalsUsed() != null ? Arrays.asList(entity.getGoalsUsed()) : List.of(),
            entity.getExperienceLevel(),
            entity.getCreatedAt(),
            cached
        );
    }
}

