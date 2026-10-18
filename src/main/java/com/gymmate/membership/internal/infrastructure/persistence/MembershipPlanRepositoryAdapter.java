package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.membership.internal.application.port.MembershipPlanRepository;
import com.gymmate.membership.internal.domain.MembershipPlan;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link MembershipPlanRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the MembershipPlan finders live here.
 */
@Component
@Transactional()
public class MembershipPlanRepositoryAdapter extends JpaDomainRepositoryAdapter<MembershipPlan, UUID, MembershipPlanJpaRepository>
        implements MembershipPlanRepository {

    public MembershipPlanRepositoryAdapter(MembershipPlanJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
    public boolean existsByGymIdAndName(UUID gymId, String name) {
        return jpaRepository.existsByGymIdAndName(gymId, name);
    }
}
