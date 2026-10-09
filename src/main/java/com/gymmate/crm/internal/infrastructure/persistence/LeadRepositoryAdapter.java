package com.gymmate.crm.internal.infrastructure.persistence;

import com.gymmate.crm.internal.application.port.LeadRepository;
import com.gymmate.crm.internal.domain.Lead;
import com.gymmate.crm.internal.domain.LeadStatus;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Persistence adapter implementing {@link LeadRepository}; CRUD comes from
 * {@link JpaDomainRepositoryAdapter}, only the lead finders live here.
 */
@Component
@Transactional
public class LeadRepositoryAdapter extends JpaDomainRepositoryAdapter<Lead, UUID, LeadJpaRepository>
        implements LeadRepository {

    public LeadRepositoryAdapter(LeadJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<Lead> findByGymId(UUID gymId) {
        return fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<Lead> findByOrganisationId(UUID organisationId) {
        return fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public List<Lead> findByGymIdAndStatus(UUID gymId, LeadStatus status) {
        return fromJpa(jpaRepository.findByGymIdAndStatus(gymId, status));
    }

    @Override
    public List<Lead> findByOrganisationIdAndStatus(UUID organisationId, LeadStatus status) {
        return fromJpa(jpaRepository.findByOrganisationIdAndStatus(organisationId, status));
    }

    @Override
    public long countByOrganisationIdAndStatus(UUID organisationId, LeadStatus status) {
        return jpaRepository.countByOrganisationIdAndStatus(organisationId, status);
    }
}
