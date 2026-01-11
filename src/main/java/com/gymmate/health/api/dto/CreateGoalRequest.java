package com.gymmate.health.api.dto;

import com.gymmate.health.domain.Enums.GoalType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Request DTO for creating a fitness goal.
 */
public record CreateGoalRequest(
    @NotNull(message = "Member ID is required")
    UUID memberId,

    @NotNull(message = "Goal type is required")
    GoalType goalType,

    @NotBlank(message = "Title is required")
    String title,

    String description,

    BigDecimal targetValue,

    String targetUnit,

    BigDecimal startValue,

    @NotNull(message = "Start date is required")
    LocalDate startDate,

    LocalDate deadlineDate
) {}
