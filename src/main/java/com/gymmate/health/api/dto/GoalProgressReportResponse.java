package com.gymmate.health.api.dto;

import com.gymmate.health.application.FitnessGoalService;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Response DTO for detailed goal progress report.
 */
public record GoalProgressReportResponse(
    FitnessGoalResponse goal,
    BigDecimal progressPercentage,
    long daysElapsed,
    long daysRemaining,
    boolean isOverdue,
    boolean targetReached,
    LocalDate estimatedCompletionDate
) {
    public static GoalProgressReportResponse from(FitnessGoalService.GoalProgressReport report) {
        return new GoalProgressReportResponse(
            FitnessGoalResponse.from(report.goal()),
            report.progressPercentage(),
            report.daysElapsed(),
            report.daysRemaining(),
            report.isOverdue(),
            report.targetReached(),
            report.estimatedCompletionDate()
        );
    }
}
