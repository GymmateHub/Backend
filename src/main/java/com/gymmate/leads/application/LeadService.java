package com.gymmate.leads.application;

import com.gymmate.leads.domain.Lead;
import com.gymmate.leads.domain.LeadStatus;
import com.gymmate.leads.infrastructure.LeadRepository;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

/**
 * Application service for lead management use cases.
 * Leads are gym-scoped: a tenant (organisation) managing multiple gyms
 * has an independent lead pipeline per gym.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class LeadService {

  private final LeadRepository leadRepository;

  /**
   * Create a new lead.
   */
  @Transactional
  public Lead createLead(Lead lead) {
    // Prevent duplicate leads for the same email within the same gym
    if (lead.getEmail() != null && !lead.getEmail().isBlank() && lead.getGymId() != null) {
      leadRepository.findByGymIdAndEmail(lead.getGymId(), lead.getEmail())
        .ifPresent(existing -> {
          throw new DomainException("DUPLICATE_LEAD_EMAIL",
            "A lead with this email already exists for this gym");
        });
    }

    log.info("Creating lead: {} for gym: {} organisation: {}",
      lead.getFullName(), lead.getGymId(), lead.getOrganisationId());
    return leadRepository.save(lead);
  }

  /**
   * Get lead by ID.
   */
  public Lead getLeadById(UUID id) {
    return leadRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("Lead", id.toString()));
  }

  /**
   * Get all leads for a gym.
   */
  public List<Lead> getLeadsByGym(UUID gymId) {
    return leadRepository.findByGymId(gymId);
  }

  /**
   * Get all leads across the organisation (all gyms of the tenant).
   */
  public List<Lead> getLeadsByOrganisation(UUID organisationId) {
    return leadRepository.findByOrganisationId(organisationId);
  }

  /**
   * Get leads for a gym filtered by status.
   */
  public List<Lead> getLeadsByGymAndStatus(UUID gymId, LeadStatus status) {
    return leadRepository.findByGymIdAndStatus(gymId, status);
  }

  /**
   * Update lead contact/follow-up details.
   */
  @Transactional
  public Lead updateLead(UUID id, Lead updated) {
    Lead existing = getLeadById(id);

    // Validate email uniqueness within the gym if changed
    if (updated.getEmail() != null && !updated.getEmail().isBlank()
        && !updated.getEmail().equalsIgnoreCase(existing.getEmail())
        && existing.getGymId() != null) {
      leadRepository.findByGymIdAndEmail(existing.getGymId(), updated.getEmail())
        .ifPresent(duplicate -> {
          throw new DomainException("DUPLICATE_LEAD_EMAIL",
            "A lead with this email already exists for this gym");
        });
    }

    if (updated.getFirstName() != null) existing.setFirstName(updated.getFirstName());
    if (updated.getLastName() != null) existing.setLastName(updated.getLastName());
    if (updated.getEmail() != null) existing.setEmail(updated.getEmail());
    if (updated.getPhone() != null) existing.setPhone(updated.getPhone());
    if (updated.getSource() != null) existing.setSource(updated.getSource());
    if (updated.getNotes() != null) existing.setNotes(updated.getNotes());
    if (updated.getAssignedTo() != null) existing.setAssignedTo(updated.getAssignedTo());
    if (updated.getFollowUpDate() != null) existing.setFollowUpDate(updated.getFollowUpDate());

    log.info("Updating lead: {}", id);
    return leadRepository.save(existing);
  }

  /**
   * Update lead status.
   */
  @Transactional
  public Lead updateLeadStatus(UUID id, LeadStatus status) {
    Lead lead = getLeadById(id);

    try {
      lead.updateStatus(status);
    } catch (IllegalStateException e) {
      throw new DomainException("INVALID_LEAD_STATUS_TRANSITION", e.getMessage());
    }

    log.info("Updating lead {} status to {}", id, status);
    return leadRepository.save(lead);
  }

  /**
   * Convert a lead into a member.
   * Marks the lead CONVERTED; optionally links the created member.
   */
  @Transactional
  public Lead convertLead(UUID id, UUID memberId) {
    Lead lead = getLeadById(id);

    try {
      lead.convert(memberId);
    } catch (IllegalStateException e) {
      throw new DomainException("LEAD_ALREADY_CONVERTED", e.getMessage());
    }

    log.info("Converted lead: {} (member: {})", id, memberId);
    return leadRepository.save(lead);
  }

  /**
   * Soft-delete a lead (preserves pipeline history).
   */
  @Transactional
  public void deleteLead(UUID id) {
    Lead lead = getLeadById(id);
    lead.deactivate();
    leadRepository.save(lead);
    log.info("Deactivated lead: {}", id);
  }

  /**
   * Count leads for a gym by status.
   */
  public long countByStatus(UUID gymId, LeadStatus status) {
    return leadRepository.countByGymIdAndStatus(gymId, status);
  }
}
