package com.gymmate.ai.api.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Size;

import java.util.List;

/**
 * Request body for on-demand AI plan generation.
 * Both fields are optional — if omitted the member's current saved goals
 * are used instead.
 */
@Schema(description = "Request to generate a new personalised AI fitness plan")
public record AiPlanRequest(

    @Size(min = 1, max = 10, message = "Provide between 1 and 10 fitness goals")
    @Schema(
        description = "Override fitness goals for this generation. "
            + "When provided these replace the member's stored goals.",
        example = "[\"Lose 10 kg\", \"Build core strength\"]"
    )
    List<String> fitnessGoals,

    @Size(max = 30)
    @Schema(
        description = "Experience level override: beginner, intermediate or advanced.",
        example = "intermediate"
    )
    String experienceLevel
) {}

