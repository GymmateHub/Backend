package com.gymmate.leads.infrastructure;

import com.gymmate.leads.domain.Lead;
import com.gymmate.leads.domain.LeadStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter for Lead repository.
 */
@Component
@RequiredArgsConstructor
public class LeadRepositoryAdapter implements LeadRepository {

  private final LeadJpaRepository jpaRepository;

  @Override
  public Lead save(Lead lead) {
    return jpaRepository.save(lead);
  }

  @Override
  public Optional<Lead> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<Lead> findByGymId(UUID gymId) {
    return jpaRepository.findByGymId(gymId);
  }

  @Override
  public List<Lead> findByOrganisationId(UUID organisationId) {
    return jpaRepository.findByOrganisationId(organisationId);
  }

  @Override
  public List<Lead> findByGymIdAndStatus(UUID gymId, LeadStatus status) {
    return jpaRepository.findByGymIdAndStatus(gymId, status);
  }

  @Override
  public Optional<Lead> findByGymIdAndEmail(UUID gymId, String email) {
    return jpaRepository.findByGymIdAndEmail(gymId, email);
  }

  @Override
  public long countByGymIdAndStatus(UUID gymId, LeadStatus status) {
    return jpaRepository.countByGymIdAndStatus(gymId, status);
  }

  @Override
  public void delete(Lead lead) {
    jpaRepository.delete(lead);
  }
}
