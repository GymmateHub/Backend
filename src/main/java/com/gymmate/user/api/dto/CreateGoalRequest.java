package com.gymmate.user.api.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Request DTO for a member creating their own fitness goal.
 * goalType accepts the GoalType enum names (case-insensitive),
 * e.g. WEIGHT_LOSS, MUSCLE_GAIN, GENERAL_FITNESS.
 */
public record CreateGoalRequest(
    @NotBlank(message = "Goal type is required") String goalType,
    @NotBlank(message = "Title is required") @Size(max = 200) String title,
    String description,
    BigDecimal targetValue,
    @Size(max = 20) String targetUnit,
    BigDecimal startValue,
    LocalDate deadlineDate) {
}
