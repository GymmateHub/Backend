package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.FitnessGoal;
import com.gymmate.health.domain.FitnessGoalRepository;
import com.gymmate.health.domain.GoalStatus;
import com.gymmate.health.domain.GoalType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing FitnessGoalRepository using JPA.
 * Bridges domain layer with infrastructure layer.
 */
@Component
@RequiredArgsConstructor
public class FitnessGoalRepositoryAdapter implements FitnessGoalRepository {

    private final FitnessGoalJpaRepository jpaRepository;

    @Override
    public FitnessGoal save(FitnessGoal fitnessGoal) {
        return jpaRepository.save(fitnessGoal);
    }

    @Override
    public Optional<FitnessGoal> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<FitnessGoal> findByMemberId(UUID memberId) {
        return jpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId);
    }

    @Override
    public List<FitnessGoal> findActiveByMemberId(UUID memberId) {
        return jpaRepository.findActiveByMemberId(memberId);
    }

    @Override
    public List<FitnessGoal> findByMemberIdAndStatus(UUID memberId, GoalStatus status) {
        return jpaRepository.findByMemberIdAndStatus(memberId, status);
    }

    @Override
    public List<FitnessGoal> findByMemberIdAndGoalType(UUID memberId, GoalType goalType) {
        return jpaRepository.findByMemberIdAndGoalType(memberId, goalType);
    }

    @Override
    public List<FitnessGoal> findOverdueGoals() {
        return jpaRepository.findOverdueGoals();
    }

    @Override
    public List<FitnessGoal> findOverdueGoalsByGymId(UUID gymId) {
        return jpaRepository.findOverdueGoalsByGymId(gymId);
    }

    @Override
    public List<FitnessGoal> findGoalsWithUpcomingDeadlines(UUID gymId, int days) {
        LocalDate deadlineDate = LocalDate.now().plusDays(days);
        return jpaRepository.findGoalsWithUpcomingDeadlines(gymId, deadlineDate);
    }

    @Override
    public long countByMemberIdAndStatus(UUID memberId, GoalStatus status) {
        return jpaRepository.countByMemberIdAndStatus(memberId, status);
    }

    @Override
    public void delete(FitnessGoal fitnessGoal) {
        fitnessGoal.setActive(false);
        jpaRepository.save(fitnessGoal);
    }
}
