package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.SoftDeletingJpaDomainRepositoryAdapter;
import com.gymmate.health.internal.application.port.FitnessGoalRepository;
import com.gymmate.health.internal.domain.FitnessGoal;
import com.gymmate.health.internal.domain.enums.GoalStatus;
import com.gymmate.health.internal.domain.enums.GoalType;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link FitnessGoalRepository} with Spring Data JPA; CRUD (with soft
 * delete) comes from {@link SoftDeletingJpaDomainRepositoryAdapter}, only the FitnessGoal finders live here.
 */
@Component
@Transactional()
public class FitnessGoalRepositoryAdapter extends SoftDeletingJpaDomainRepositoryAdapter<FitnessGoal, UUID, FitnessGoalJpaRepository>
        implements FitnessGoalRepository {

    public FitnessGoalRepositoryAdapter(FitnessGoalJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<FitnessGoal> findByMemberId(UUID memberId) {
        return this.<List<FitnessGoal>>fromJpa(jpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId));
    }

    @Override
    public List<FitnessGoal> findActiveByMemberId(UUID memberId) {
        return this.<List<FitnessGoal>>fromJpa(jpaRepository.findActiveByMemberId(memberId));
    }

    @Override
    public List<FitnessGoal> findByMemberIdAndStatus(UUID memberId, GoalStatus status) {
        return this.<List<FitnessGoal>>fromJpa(jpaRepository.findByMemberIdAndStatus(memberId, status));
    }

    @Override
    public List<FitnessGoal> findByMemberIdAndGoalType(UUID memberId, GoalType goalType) {
        return this.<List<FitnessGoal>>fromJpa(jpaRepository.findByMemberIdAndGoalType(memberId, goalType));
    }

    @Override
    public List<FitnessGoal> findOverdueGoals() {
        return this.<List<FitnessGoal>>fromJpa(jpaRepository.findOverdueGoals());
    }

    @Override
    public List<FitnessGoal> findOverdueGoalsByGymId(UUID gymId) {
        return this.<List<FitnessGoal>>fromJpa(jpaRepository.findOverdueGoalsByGymId(gymId));
    }

    @Override
    public List<FitnessGoal> findGoalsWithUpcomingDeadlines(UUID gymId, int days) {
        LocalDate deadlineDate = LocalDate.now().plusDays(days);
        return this.<List<FitnessGoal>>fromJpa(jpaRepository.findGoalsWithUpcomingDeadlines(gymId, deadlineDate));
    }

    @Override
    public long countByMemberIdAndStatus(UUID memberId, GoalStatus status) {
        return jpaRepository.countByMemberIdAndStatus(memberId, status);
    }

    @Override
    public List<FitnessGoal> findByMemberIdOrderByCreatedAtDesc(UUID memberId) {
        return this.<List<FitnessGoal>>fromJpa(jpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId));
    }

    @Override
    public List<FitnessGoal> findGoalsWithUpcomingDeadlines(UUID gymId, LocalDate deadlineDate) {
        return this.<List<FitnessGoal>>fromJpa(jpaRepository.findGoalsWithUpcomingDeadlines(gymId, deadlineDate));
    }
}
