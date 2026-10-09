package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.access.internal.domain.DoorBenefit;
import java.util.UUID;
import com.gymmate.access.internal.application.port.DoorBenefitRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link DoorBenefitRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the DoorBenefit finders live here.
 */
@Component()
@Transactional()
public class DoorBenefitRepositoryAdapter extends JpaDomainRepositoryAdapter<DoorBenefit, UUID, DoorBenefitJpaRepository>
        implements DoorBenefitRepository {

    public DoorBenefitRepositoryAdapter(DoorBenefitJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public boolean existsByAccessPointId(UUID accessPointId) {
        return jpaRepository.existsByAccessPointId(accessPointId);
    }

    @Override
    public boolean existsByAccessPointIdAndMembershipPlanId(UUID accessPointId, UUID membershipPlanId) {
        return jpaRepository.existsByAccessPointIdAndMembershipPlanId(accessPointId, membershipPlanId);
    }
}
