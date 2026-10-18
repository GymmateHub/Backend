package com.gymmate.crm.internal.application.port;

import com.gymmate.crm.internal.domain.Lead;
import com.gymmate.crm.internal.domain.LeadStatus;
import com.gymmate.shared.application.port.DomainRepository;

import java.util.List;
import java.util.UUID;

public interface LeadRepository extends DomainRepository<Lead, UUID> {
    List<Lead> findByGymId(UUID gymId);
    List<Lead> findByOrganisationId(UUID organisationId);
    List<Lead> findByGymIdAndStatus(UUID gymId, LeadStatus status);
    List<Lead> findByOrganisationIdAndStatus(UUID organisationId, LeadStatus status);
    long countByOrganisationIdAndStatus(UUID organisationId, LeadStatus status);
}
