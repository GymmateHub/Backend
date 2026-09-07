package com.gymmate.lead.application;

import com.gymmate.lead.api.dto.LeadCreateRequest;
import com.gymmate.lead.api.dto.LeadUpdateRequest;
import com.gymmate.lead.domain.Lead;
import com.gymmate.lead.domain.LeadStatus;
import com.gymmate.lead.infrastructure.LeadRepository;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.multitenancy.TenantContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LeadService {

    private final LeadRepository leadRepository;

    @Transactional
    public Lead createLead(UUID gymId, LeadCreateRequest request) {
        UUID orgId = TenantContext.requireCurrentTenantId();
        Lead lead = Lead.builder()
                .firstName(request.firstName().trim())
                .lastName(request.lastName().trim())
                .email(request.email() != null ? request.email().trim() : null)
                .phone(request.phone() != null ? request.phone().trim() : null)
                .source(request.source())
                .notes(request.notes())
                .assignedTo(request.assignedTo())
                .followUpDate(request.followUpDate())
                .status(LeadStatus.NEW)
                .build();

        lead.setGymId(gymId);
        lead.setOrganisationId(orgId);

        return leadRepository.save(lead);
    }

    public Lead findById(UUID id) {
        return leadRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Lead", id.toString()));
    }

    public List<Lead> findByGymId(UUID gymId) {
        return leadRepository.findByGymId(gymId);
    }

    public List<Lead> findByOrganisationId(UUID organisationId) {
        return leadRepository.findByOrganisationId(organisationId);
    }

    public List<Lead> findByGymIdAndStatus(UUID gymId, LeadStatus status) {
        return leadRepository.findByGymIdAndStatus(gymId, status);
    }

    @Transactional
    public Lead updateLead(UUID id, LeadUpdateRequest request) {
        Lead lead = findById(id);
        lead.updateDetails(
                request.firstName(),
                request.lastName(),
                request.email(),
                request.phone(),
                request.source(),
                request.notes(),
                request.assignedTo(),
                request.followUpDate()
        );
        return leadRepository.save(lead);
    }

    @Transactional
    public Lead updateStatus(UUID id, LeadStatus status) {
        Lead lead = findById(id);
        lead.updateStatus(status);
        return leadRepository.save(lead);
    }

    @Transactional
    public Lead convert(UUID id, UUID memberId) {
        Lead lead = findById(id);
        lead.convert(memberId);
        return leadRepository.save(lead);
    }

    @Transactional
    public void deleteLead(UUID id) {
        Lead lead = findById(id);
        leadRepository.delete(lead);
    }
}
