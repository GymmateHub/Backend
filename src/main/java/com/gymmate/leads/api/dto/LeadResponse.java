package com.gymmate.leads.api.dto;

import com.gymmate.leads.domain.Lead;
import com.gymmate.leads.domain.LeadStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for lead data. Field names match the frontend contract
 * (frontend/src/features/leads/leads.api.ts).
 */
public record LeadResponse(
    UUID id,
    UUID gymId,
    String firstName,
    String lastName,
    String email,
    String phone,
    String source,
    LeadStatus status,
    String notes,
    UUID assignedTo,
    LocalDate followUpDate,
    LocalDateTime convertedAt,
    UUID convertedMemberId,
    LocalDateTime createdAt,
    LocalDateTime updatedAt) {

  public static LeadResponse fromEntity(Lead lead) {
    return new LeadResponse(
        lead.getId(),
        lead.getGymId(),
        lead.getFirstName(),
        lead.getLastName(),
        lead.getEmail(),
        lead.getPhone(),
        lead.getSource(),
        lead.getStatus(),
        lead.getNotes(),
        lead.getAssignedTo(),
        lead.getFollowUpDate(),
        lead.getConvertedAt(),
        lead.getConvertedMemberId(),
        lead.getCreatedAt(),
        lead.getUpdatedAt());
  }
}
