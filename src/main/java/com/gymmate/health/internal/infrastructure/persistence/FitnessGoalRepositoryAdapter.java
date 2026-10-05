package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.health.internal.application.port.FitnessGoalRepository;
import com.gymmate.health.internal.domain.FitnessGoal;
import com.gymmate.health.internal.domain.enums.GoalStatus;
import com.gymmate.health.internal.domain.enums.GoalType;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link FitnessGoalRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class FitnessGoalRepositoryAdapter extends DomainRepositoryAdapter implements FitnessGoalRepository {

    private final FitnessGoalJpaRepository jpaRepository;

    public FitnessGoalRepositoryAdapter(FitnessGoalJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public FitnessGoal save(FitnessGoal fitnessGoal) {
        return save(jpaRepository, fitnessGoal);
    }

    @Override
    public Optional<FitnessGoal> findById(UUID id) {
        return this.<Optional<FitnessGoal>>fromJpa(jpaRepository.findById(id));
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
    public void delete(FitnessGoal fitnessGoal) {
        fitnessGoal.setActive(false);
        save(jpaRepository, fitnessGoal);
    }

    @Override
    public List<FitnessGoal> findByMemberIdOrderByCreatedAtDesc(UUID memberId) {
        return this.<List<FitnessGoal>>fromJpa(jpaRepository.findByMemberIdOrderByCreatedAtDesc(memberId));
    }

    @Override
    public List<FitnessGoal> findGoalsWithUpcomingDeadlines(UUID gymId, LocalDate deadlineDate) {
        return this.<List<FitnessGoal>>fromJpa(jpaRepository.findGoalsWithUpcomingDeadlines(gymId, deadlineDate));
    }

    @Override
    public List<FitnessGoal> saveAll(Iterable<FitnessGoal> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<FitnessGoal> findAll() {
        return this.<List<FitnessGoal>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<FitnessGoal> findAllById(Iterable<UUID> ids) {
        return this.<List<FitnessGoal>>fromJpa(jpaRepository.findAllById(ids));
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteAll(Iterable<FitnessGoal> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public FitnessGoal saveAndFlush(FitnessGoal entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<FitnessGoal> findAll(Pageable pageable) {
        return this.<Page<FitnessGoal>>fromJpa(jpaRepository.findAll(pageable));
    }
}
