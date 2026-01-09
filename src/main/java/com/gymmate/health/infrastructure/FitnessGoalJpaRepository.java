package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.FitnessGoal;
import com.gymmate.health.domain.GoalStatus;
import com.gymmate.health.domain.GoalType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

/**
 * JPA repository for FitnessGoal entity.
 * Provides data access operations using Spring Data JPA.
 */
@Repository
public interface FitnessGoalJpaRepository extends JpaRepository<FitnessGoal, UUID> {

    /**
     * Find all goals for a member ordered by creation date descending.
     */
    @Query("SELECT fg FROM FitnessGoal fg WHERE fg.memberId = :memberId AND fg.active = true ORDER BY fg.createdAt DESC")
    List<FitnessGoal> findByMemberIdOrderByCreatedAtDesc(@Param("memberId") UUID memberId);

    /**
     * Find active goals for a member.
     */
    @Query("SELECT fg FROM FitnessGoal fg WHERE fg.memberId = :memberId AND fg.status = 'ACTIVE' AND fg.active = true ORDER BY fg.deadlineDate ASC NULLS LAST")
    List<FitnessGoal> findActiveByMemberId(@Param("memberId") UUID memberId);

    /**
     * Find goals by member and status.
     */
    @Query("SELECT fg FROM FitnessGoal fg WHERE fg.memberId = :memberId AND fg.status = :status AND fg.active = true ORDER BY fg.createdAt DESC")
    List<FitnessGoal> findByMemberIdAndStatus(
        @Param("memberId") UUID memberId,
        @Param("status") GoalStatus status
    );

    /**
     * Find goals by member and goal type.
     */
    @Query("SELECT fg FROM FitnessGoal fg WHERE fg.memberId = :memberId AND fg.goalType = :goalType AND fg.active = true ORDER BY fg.createdAt DESC")
    List<FitnessGoal> findByMemberIdAndGoalType(
        @Param("memberId") UUID memberId,
        @Param("goalType") GoalType goalType
    );

    /**
     * Find overdue goals (deadline passed but still active).
     */
    @Query("SELECT fg FROM FitnessGoal fg WHERE fg.status = 'ACTIVE' AND fg.deadlineDate < CURRENT_DATE AND fg.active = true ORDER BY fg.deadlineDate ASC")
    List<FitnessGoal> findOverdueGoals();

    /**
     * Find overdue goals by gym.
     */
    @Query("SELECT fg FROM FitnessGoal fg WHERE fg.gymId = :gymId AND fg.status = 'ACTIVE' AND fg.deadlineDate < CURRENT_DATE AND fg.active = true ORDER BY fg.deadlineDate ASC")
    List<FitnessGoal> findOverdueGoalsByGymId(@Param("gymId") UUID gymId);

    /**
     * Find goals with deadlines within next N days.
     */
    @Query("SELECT fg FROM FitnessGoal fg WHERE fg.gymId = :gymId AND fg.status = 'ACTIVE' AND fg.deadlineDate BETWEEN CURRENT_DATE AND :deadlineDate AND fg.active = true ORDER BY fg.deadlineDate ASC")
    List<FitnessGoal> findGoalsWithUpcomingDeadlines(
        @Param("gymId") UUID gymId,
        @Param("deadlineDate") LocalDate deadlineDate
    );

    /**
     * Count goals by member and status.
     */
    @Query("SELECT COUNT(fg) FROM FitnessGoal fg WHERE fg.memberId = :memberId AND fg.status = :status AND fg.active = true")
    long countByMemberIdAndStatus(
        @Param("memberId") UUID memberId,
        @Param("status") GoalStatus status
    );
}
