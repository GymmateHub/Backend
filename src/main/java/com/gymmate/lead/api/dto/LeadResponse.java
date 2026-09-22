package com.gymmate.lead.api.dto;

import com.gymmate.lead.domain.Lead;
import com.gymmate.lead.domain.LeadStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record LeadResponse(
    UUID id,
    UUID gymId,
    UUID organisationId,
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
    LocalDateTime updatedAt
) {
    public static LeadResponse fromEntity(Lead lead) {
        return new LeadResponse(
            lead.getId(),
            lead.getGymId(),
            lead.getOrganisationId(),
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
            lead.getUpdatedAt()
        );
    }
}
