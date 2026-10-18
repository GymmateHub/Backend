package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.membership.internal.application.port.FreezePolicyRepository;
import com.gymmate.membership.internal.domain.FreezePolicy;
import org.springframework.stereotype.Component;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link FreezePolicyRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the FreezePolicy finders live here.
 */
@Component
@Transactional()
public class FreezePolicyRepositoryAdapter extends JpaDomainRepositoryAdapter<FreezePolicy, UUID, FreezePolicyJpaRepository>
        implements FreezePolicyRepository {

    public FreezePolicyRepositoryAdapter(FreezePolicyJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public Optional<FreezePolicy> findActiveByGymId(UUID gymId) {
        return this.<Optional<FreezePolicy>>fromJpa(jpaRepository.findActiveByGymId(gymId));
    }

    @Override
    public Optional<FreezePolicy> findDefaultPolicy() {
        return this.<Optional<FreezePolicy>>fromJpa(jpaRepository.findDefaultPolicy());
    }

    @Override
    public Optional<FreezePolicy> findDefaultPolicyByOrganisation(UUID organisationId) {
        return this.<Optional<FreezePolicy>>fromJpa(jpaRepository.findDefaultPolicyByOrganisation(organisationId));
    }

    @Override
    public Optional<FreezePolicy> findByGymIdAndActiveTrue(UUID gymId) {
        return this.<Optional<FreezePolicy>>fromJpa(jpaRepository.findByGymIdAndActiveTrue(gymId));
    }

    @Override
    public Optional<FreezePolicy> findByOrganisationIdAndIsDefaultPolicyTrueAndActiveTrue(UUID organisationId) {
        return this.<Optional<FreezePolicy>>fromJpa(jpaRepository.findByOrganisationIdAndIsDefaultPolicyTrueAndActiveTrue(organisationId));
    }
}
