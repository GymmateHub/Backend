package com.gymmate.lead.infrastructure;

import com.gymmate.lead.domain.Lead;
import com.gymmate.lead.domain.LeadStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LeadRepository extends JpaRepository<Lead, UUID> {
    List<Lead> findByGymId(UUID gymId);
    List<Lead> findByOrganisationId(UUID organisationId);
    List<Lead> findByGymIdAndStatus(UUID gymId, LeadStatus status);
    List<Lead> findByOrganisationIdAndStatus(UUID organisationId, LeadStatus status);
    long countByOrganisationIdAndStatus(UUID organisationId, LeadStatus status);
}
