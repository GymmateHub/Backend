package com.gymmate.health.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for FitnessGoal.
 * Defines domain-level operations for managing fitness goals.
 */
public interface FitnessGoalRepository {

    /**
     * Save or update a fitness goal.
     */
    FitnessGoal save(FitnessGoal fitnessGoal);

    /**
     * Find fitness goal by ID.
     */
    Optional<FitnessGoal> findById(UUID id);

    /**
     * Find all goals for a member.
     */
    List<FitnessGoal> findByMemberId(UUID memberId);

    /**
     * Find active goals for a member.
     */
    List<FitnessGoal> findActiveByMemberId(UUID memberId);

    /**
     * Find goals by member and status.
     */
    List<FitnessGoal> findByMemberIdAndStatus(UUID memberId, GoalStatus status);

    /**
     * Find goals by member and goal type.
     */
    List<FitnessGoal> findByMemberIdAndGoalType(UUID memberId, GoalType goalType);

    /**
     * Find overdue goals (deadline passed but still active).
     */
    List<FitnessGoal> findOverdueGoals();

    /**
     * Find overdue goals by gym.
     */
    List<FitnessGoal> findOverdueGoalsByGymId(UUID gymId);

    /**
     * Find goals with upcoming deadlines (within next N days).
     */
    List<FitnessGoal> findGoalsWithUpcomingDeadlines(UUID gymId, int days);

    /**
     * Count goals by member and status.
     */
    long countByMemberIdAndStatus(UUID memberId, GoalStatus status);

    /**
     * Delete a fitness goal (soft delete).
     */
    void delete(FitnessGoal fitnessGoal);
}
