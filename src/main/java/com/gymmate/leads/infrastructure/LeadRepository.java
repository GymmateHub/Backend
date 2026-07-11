package com.gymmate.leads.infrastructure;

import com.gymmate.leads.domain.Lead;
import com.gymmate.leads.domain.LeadStatus;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Lead domain entity.
 */
public interface LeadRepository {

  Lead save(Lead lead);

  Optional<Lead> findById(UUID id);

  List<Lead> findByGymId(UUID gymId);

  List<Lead> findByOrganisationId(UUID organisationId);

  List<Lead> findByGymIdAndStatus(UUID gymId, LeadStatus status);

  Optional<Lead> findByGymIdAndEmail(UUID gymId, String email);

  long countByGymIdAndStatus(UUID gymId, LeadStatus status);

  void delete(Lead lead);
}
