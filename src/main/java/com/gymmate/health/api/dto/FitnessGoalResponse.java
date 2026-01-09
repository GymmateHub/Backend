package com.gymmate.health.api.dto;

import com.gymmate.health.domain.FitnessGoal;
import com.gymmate.health.domain.GoalStatus;
import com.gymmate.health.domain.GoalType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for FitnessGoal.
 */
public record FitnessGoalResponse(
    UUID id,
    UUID memberId,
    GoalType goalType,
    String title,
    String description,
    BigDecimal targetValue,
    String targetUnit,
    BigDecimal startValue,
    BigDecimal currentValue,
    LocalDate startDate,
    LocalDate deadlineDate,
    LocalDate achievedDate,
    GoalStatus status,
    BigDecimal progressPercentage,
    long daysRemaining,
    boolean isOverdue,
    LocalDateTime createdAt
) {
    public static FitnessGoalResponse from(FitnessGoal goal) {
        return new FitnessGoalResponse(
            goal.getId(),
            goal.getMemberId(),
            goal.getGoalType(),
            goal.getTitle(),
            goal.getDescription(),
            goal.getTargetValue(),
            goal.getTargetUnit(),
            goal.getStartValue(),
            goal.getCurrentValue(),
            goal.getStartDate(),
            goal.getDeadlineDate(),
            goal.getAchievedDate(),
            goal.getStatus(),
            goal.calculateProgress(),
            goal.getDaysRemaining(),
            goal.isOverdue(),
            goal.getCreatedAt()
        );
    }
}
