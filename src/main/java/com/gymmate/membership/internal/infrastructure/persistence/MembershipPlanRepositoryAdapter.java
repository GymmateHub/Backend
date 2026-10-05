package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.membership.internal.application.port.MembershipPlanRepository;
import com.gymmate.membership.internal.domain.MembershipPlan;
import org.springframework.stereotype.Component;
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
 * Persistence adapter implementing {@link MembershipPlanRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class MembershipPlanRepositoryAdapter extends DomainRepositoryAdapter implements MembershipPlanRepository {

    private final MembershipPlanJpaRepository jpaRepository;

    public MembershipPlanRepositoryAdapter(MembershipPlanJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MembershipPlan save(MembershipPlan membershipPlan) {
        return save(jpaRepository, membershipPlan);
    }

    @Override
    public Optional<MembershipPlan> findById(UUID id) {
        return this.<Optional<MembershipPlan>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public List<MembershipPlan> findByGymId(UUID gymId) {
        return this.<List<MembershipPlan>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<MembershipPlan> findActiveByGymId(UUID gymId) {
        return this.<List<MembershipPlan>>fromJpa(jpaRepository.findActiveByGymId(gymId));
    }

    @Override
    public List<MembershipPlan> findFeaturedByGymId(UUID gymId) {
        return this.<List<MembershipPlan>>fromJpa(jpaRepository.findFeaturedByGymId(gymId));
    }

    @Override
    public Optional<MembershipPlan> findByGymIdAndName(UUID gymId, String name) {
        return this.<Optional<MembershipPlan>>fromJpa(jpaRepository.findByGymIdAndName(gymId, name));
    }

    @Override
    public void delete(MembershipPlan membershipPlan) {
        delete(jpaRepository, membershipPlan);
    }

    @Override
    public boolean existsByGymIdAndName(UUID gymId, String name) {
        return jpaRepository.existsByGymIdAndName(gymId, name);
    }

    @Override
    public List<MembershipPlan> saveAll(Iterable<MembershipPlan> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<MembershipPlan> findAll() {
        return this.<List<MembershipPlan>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<MembershipPlan> findAllById(Iterable<UUID> ids) {
        return this.<List<MembershipPlan>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<MembershipPlan> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public MembershipPlan saveAndFlush(MembershipPlan entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<MembershipPlan> findAll(Pageable pageable) {
        return this.<Page<MembershipPlan>>fromJpa(jpaRepository.findAll(pageable));
    }
}
