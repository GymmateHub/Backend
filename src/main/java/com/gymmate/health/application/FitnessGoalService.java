package com.gymmate.health.application;

import com.gymmate.health.domain.*;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * Application service for fitness goals management.
 * Handles goal creation, progress tracking, achievement, and reporting.
 * Implements FR-015: Health Insights & Goals (Goal Tracking).
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class FitnessGoalService {

    private final FitnessGoalRepository fitnessGoalRepository;

    /**
     * Create a new fitness goal.
     */
    @Transactional
    public FitnessGoal createGoal(
            UUID organisationId,
            UUID gymId,
            UUID memberId,
            GoalType goalType,
            String title,
            String description,
            BigDecimal targetValue,
            String targetUnit,
            BigDecimal startValue,
            LocalDate startDate,
            LocalDate deadlineDate
    ) {
        log.info("Creating fitness goal for member {}: {}", memberId, title);

        FitnessGoal goal = FitnessGoal.builder()
            .organisationId(organisationId)
            .gymId(gymId)
            .memberId(memberId)
            .goalType(goalType)
            .title(title)
            .description(description)
            .targetValue(targetValue)
            .targetUnit(targetUnit)
            .startValue(startValue)
            .currentValue(startValue) // Initialize with start value
            .startDate(startDate)
            .deadlineDate(deadlineDate)
            .status(GoalStatus.ACTIVE)
            .build();

        // Validate goal
        goal.validate();

        FitnessGoal savedGoal = fitnessGoalRepository.save(goal);
        log.info("Successfully created fitness goal {} for member {}", savedGoal.getId(), memberId);

        return savedGoal;
    }

    /**
     * Update goal progress.
     */
    @Transactional
    public FitnessGoal updateGoalProgress(UUID goalId, BigDecimal newCurrentValue) {
        log.info("Updating progress for goal {}: {}", goalId, newCurrentValue);

        FitnessGoal goal = fitnessGoalRepository.findById(goalId)
            .orElseThrow(() -> new ResourceNotFoundException("Fitness goal not found with ID: " + goalId));

        if (goal.getStatus() != GoalStatus.ACTIVE) {
            throw new DomainException("GOAL_NOT_ACTIVE",
                "Cannot update progress for a goal that is not active. Current status: " + goal.getStatus());
        }

        // Update progress
        goal.updateProgress(newCurrentValue);

        FitnessGoal updatedGoal = fitnessGoalRepository.save(goal);

        if (updatedGoal.getStatus() == GoalStatus.ACHIEVED) {
            log.info("Goal {} has been achieved!", goalId);
        }

        return updatedGoal;
    }

    /**
     * Manually mark goal as achieved.
     */
    @Transactional
    public FitnessGoal achieveGoal(UUID goalId) {
        log.info("Manually achieving goal: {}", goalId);

        FitnessGoal goal = fitnessGoalRepository.findById(goalId)
            .orElseThrow(() -> new ResourceNotFoundException("Fitness goal not found with ID: " + goalId));

        goal.achieve();

        FitnessGoal savedGoal = fitnessGoalRepository.save(goal);
        log.info("Successfully achieved goal: {}", goalId);

        return savedGoal;
    }

    /**
     * Abandon a goal with reason.
     */
    @Transactional
    public FitnessGoal abandonGoal(UUID goalId, String reason) {
        log.info("Abandoning goal {}: {}", goalId, reason);

        FitnessGoal goal = fitnessGoalRepository.findById(goalId)
            .orElseThrow(() -> new ResourceNotFoundException("Fitness goal not found with ID: " + goalId));

        goal.abandon(reason);

        FitnessGoal savedGoal = fitnessGoalRepository.save(goal);
        log.info("Successfully abandoned goal: {}", goalId);

        return savedGoal;
    }

    /**
     * Pause a goal.
     */
    @Transactional
    public FitnessGoal pauseGoal(UUID goalId) {
        log.info("Pausing goal: {}", goalId);

        FitnessGoal goal = fitnessGoalRepository.findById(goalId)
            .orElseThrow(() -> new ResourceNotFoundException("Fitness goal not found with ID: " + goalId));

        goal.pauseGoal();

        FitnessGoal savedGoal = fitnessGoalRepository.save(goal);
        log.info("Successfully paused goal: {}", goalId);

        return savedGoal;
    }

    /**
     * Resume a paused goal.
     */
    @Transactional
    public FitnessGoal resumeGoal(UUID goalId) {
        log.info("Resuming goal: {}", goalId);

        FitnessGoal goal = fitnessGoalRepository.findById(goalId)
            .orElseThrow(() -> new ResourceNotFoundException("Fitness goal not found with ID: " + goalId));

        goal.resumeGoal();

        FitnessGoal savedGoal = fitnessGoalRepository.save(goal);
        log.info("Successfully resumed goal: {}", goalId);

        return savedGoal;
    }

    /**
     * Get goal by ID.
     */
    @Transactional(readOnly = true)
    public FitnessGoal getGoalById(UUID goalId) {
        log.debug("Fetching goal: {}", goalId);
        return fitnessGoalRepository.findById(goalId)
            .orElseThrow(() -> new ResourceNotFoundException("Fitness goal not found with ID: " + goalId));
    }

    /**
     * Get all goals for a member.
     */
    @Transactional(readOnly = true)
    public List<FitnessGoal> getMemberGoals(UUID memberId) {
        log.debug("Fetching all goals for member: {}", memberId);
        return fitnessGoalRepository.findByMemberId(memberId);
    }

    /**
     * Get active goals for a member.
     */
    @Transactional(readOnly = true)
    public List<FitnessGoal> getActiveGoals(UUID memberId) {
        log.debug("Fetching active goals for member: {}", memberId);
        return fitnessGoalRepository.findActiveByMemberId(memberId);
    }

    /**
     * Get goals by status.
     */
    @Transactional(readOnly = true)
    public List<FitnessGoal> getGoalsByStatus(UUID memberId, GoalStatus status) {
        log.debug("Fetching {} goals for member: {}", status, memberId);
        return fitnessGoalRepository.findByMemberIdAndStatus(memberId, status);
    }

    /**
     * Get goals by type.
     */
    @Transactional(readOnly = true)
    public List<FitnessGoal> getGoalsByType(UUID memberId, GoalType goalType) {
        log.debug("Fetching {} goals for member: {}", goalType, memberId);
        return fitnessGoalRepository.findByMemberIdAndGoalType(memberId, goalType);
    }

    /**
     * Get overdue goals for a gym.
     */
    @Transactional(readOnly = true)
    public List<FitnessGoal> getOverdueGoals(UUID gymId) {
        log.debug("Fetching overdue goals for gym: {}", gymId);
        return fitnessGoalRepository.findOverdueGoalsByGymId(gymId);
    }

    /**
     * Get goals with upcoming deadlines.
     */
    @Transactional(readOnly = true)
    public List<FitnessGoal> getGoalsWithUpcomingDeadlines(UUID gymId, int days) {
        log.debug("Fetching goals with deadlines in next {} days for gym: {}", days, gymId);
        return fitnessGoalRepository.findGoalsWithUpcomingDeadlines(gymId, days);
    }

    /**
     * Calculate detailed goal progress report.
     */
    @Transactional(readOnly = true)
    public GoalProgressReport calculateProgressReport(UUID goalId) {
        log.debug("Calculating progress report for goal: {}", goalId);

        FitnessGoal goal = getGoalById(goalId);

        BigDecimal progressPercentage = goal.calculateProgress();
        long daysRemaining = goal.getDaysRemaining();
        boolean isOverdue = goal.isOverdue();
        boolean targetReached = goal.isTargetReached();

        // Calculate days elapsed
        long daysElapsed = java.time.temporal.ChronoUnit.DAYS.between(goal.getStartDate(), LocalDate.now());

        // Calculate estimated completion date based on current progress rate
        LocalDate estimatedCompletion = null;
        if (goal.getCurrentValue() != null && goal.getStartValue() != null &&
            goal.getTargetValue() != null && daysElapsed > 0) {

            BigDecimal progressMade = goal.getCurrentValue().subtract(goal.getStartValue());
            BigDecimal totalProgressNeeded = goal.getTargetValue().subtract(goal.getStartValue());

            if (progressMade.compareTo(BigDecimal.ZERO) > 0 &&
                totalProgressNeeded.compareTo(BigDecimal.ZERO) > 0) {

                double rate = progressMade.doubleValue() / daysElapsed;
                double remainingProgress = totalProgressNeeded.subtract(progressMade).doubleValue();
                long daysToCompletion = (long) (remainingProgress / rate);

                estimatedCompletion = LocalDate.now().plusDays(daysToCompletion);
            }
        }

        return new GoalProgressReport(
            goal,
            progressPercentage,
            daysElapsed,
            daysRemaining,
            isOverdue,
            targetReached,
            estimatedCompletion
        );
    }

    /**
     * Get goal statistics for a member.
     */
    @Transactional(readOnly = true)
    public GoalStatistics getMemberGoalStatistics(UUID memberId) {
        log.debug("Calculating goal statistics for member: {}", memberId);

        long activeCount = fitnessGoalRepository.countByMemberIdAndStatus(memberId, GoalStatus.ACTIVE);
        long achievedCount = fitnessGoalRepository.countByMemberIdAndStatus(memberId, GoalStatus.ACHIEVED);
        long abandonedCount = fitnessGoalRepository.countByMemberIdAndStatus(memberId, GoalStatus.ABANDONED);
        long onHoldCount = fitnessGoalRepository.countByMemberIdAndStatus(memberId, GoalStatus.ON_HOLD);

        long totalCount = activeCount + achievedCount + abandonedCount + onHoldCount;

        double successRate = totalCount > 0
            ? (achievedCount * 100.0) / (achievedCount + abandonedCount)
            : 0.0;

        return new GoalStatistics(
            (int) totalCount,
            (int) activeCount,
            (int) achievedCount,
            (int) abandonedCount,
            (int) onHoldCount,
            successRate
        );
    }

    /**
     * Delete a goal.
     */
    @Transactional
    public void deleteGoal(UUID goalId) {
        log.info("Deleting goal: {}", goalId);

        FitnessGoal goal = fitnessGoalRepository.findById(goalId)
            .orElseThrow(() -> new ResourceNotFoundException("Fitness goal not found with ID: " + goalId));

        fitnessGoalRepository.delete(goal);
        log.info("Successfully deleted goal: {}", goalId);
    }

    // DTOs

    public record GoalProgressReport(
        FitnessGoal goal,
        BigDecimal progressPercentage,
        long daysElapsed,
        long daysRemaining,
        boolean isOverdue,
        boolean targetReached,
        LocalDate estimatedCompletionDate
    ) {}

    public record GoalStatistics(
        int totalGoals,
        int activeGoals,
        int achievedGoals,
        int abandonedGoals,
        int onHoldGoals,
        double successRate
    ) {}
}
