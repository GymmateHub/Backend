package com.gymmate.user.api.dto;

import com.gymmate.health.domain.FitnessGoal;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * A member's fitness goal, shaped for the mobile app.
 */
public record MemberGoalResponse(
    UUID id,
    String goalType,
    String title,
    String description,
    BigDecimal targetValue,
    String targetUnit,
    BigDecimal startValue,
    BigDecimal currentValue,
    LocalDate startDate,
    LocalDate deadlineDate,
    LocalDate achievedDate,
    String status) {

  public static MemberGoalResponse from(FitnessGoal goal) {
    return new MemberGoalResponse(
        goal.getId(),
        goal.getGoalType() != null ? goal.getGoalType().name() : null,
        goal.getTitle(),
        goal.getDescription(),
        goal.getTargetValue(),
        goal.getTargetUnit(),
        goal.getStartValue(),
        goal.getCurrentValue(),
        goal.getStartDate(),
        goal.getDeadlineDate(),
        goal.getAchievedDate(),
        goal.getStatus() != null ? goal.getStatus().name() : null);
  }
}
