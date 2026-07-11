package com.gymmate.leads.api;

import com.gymmate.leads.api.dto.*;
import com.gymmate.leads.application.LeadService;
import com.gymmate.leads.domain.Lead;
import com.gymmate.leads.domain.LeadStatus;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import com.gymmate.shared.multitenancy.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * REST controller for lead management operations.
 * Leads are gym-scoped within an organisation, so a tenant managing
 * multiple gyms keeps an independent lead pipeline per gym.
 */
@Slf4j
@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@Tag(name = "Leads", description = "Lead Management APIs")
public class LeadController {

  private final LeadService leadService;

  @PostMapping
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Create lead")
  public ResponseEntity<ApiResponse<LeadResponse>> createLead(
      @Valid @RequestBody LeadCreateRequest request) {

    Lead lead = Lead.builder()
      .firstName(request.firstName())
      .lastName(request.lastName())
      .email(request.email())
      .phone(request.phone())
      .source(request.source())
      .notes(request.notes())
      .assignedTo(request.assignedTo())
      .followUpDate(request.followUpDate())
      .build();

    lead.setOrganisationId(TenantContext.getCurrentTenantId());
    // Explicit gymId wins; otherwise GymScopedEntity resolves it from the tenant context
    if (request.gymId() != null) {
      lead.setGymId(request.gymId());
    } else {
      lead.setGymId(TenantContext.getCurrentGymId());
    }

    Lead created = leadService.createLead(lead);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success(LeadResponse.fromEntity(created), "Lead created successfully"));
  }

  @GetMapping("/{id}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get lead by ID")
  public ResponseEntity<ApiResponse<LeadResponse>> getLead(@PathVariable UUID id) {
    Lead lead = getLeadForCurrentTenant(id);
    return ResponseEntity.ok(ApiResponse.success(LeadResponse.fromEntity(lead)));
  }

  @GetMapping("/gym/{gymId}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get all leads for a gym")
  public ResponseEntity<ApiResponse<List<LeadResponse>>> getLeadsByGym(@PathVariable UUID gymId) {
    List<Lead> leads = leadService.getLeadsByGym(gymId);
    return ResponseEntity.ok(ApiResponse.success(toResponses(leads)));
  }

  @GetMapping("/gym/{gymId}/status/{status}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Get leads for a gym filtered by status")
  public ResponseEntity<ApiResponse<List<LeadResponse>>> getLeadsByGymAndStatus(
      @PathVariable UUID gymId,
      @PathVariable String status) {
    List<Lead> leads = leadService.getLeadsByGymAndStatus(gymId, parseStatus(status));
    return ResponseEntity.ok(ApiResponse.success(toResponses(leads)));
  }

  @GetMapping("/organisation")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  @Operation(summary = "Get all leads across the organisation (all gyms)")
  public ResponseEntity<ApiResponse<List<LeadResponse>>> getLeadsByOrganisation() {
    UUID organisationId = TenantContext.getCurrentTenantId();
    List<Lead> leads = leadService.getLeadsByOrganisation(organisationId);
    return ResponseEntity.ok(ApiResponse.success(toResponses(leads)));
  }

  @PutMapping("/{id}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Update lead")
  public ResponseEntity<ApiResponse<LeadResponse>> updateLead(
      @PathVariable UUID id,
      @Valid @RequestBody LeadUpdateRequest request) {

    getLeadForCurrentTenant(id);

    Lead updated = Lead.builder()
      .firstName(request.firstName())
      .lastName(request.lastName())
      .email(request.email())
      .phone(request.phone())
      .source(request.source())
      .notes(request.notes())
      .assignedTo(request.assignedTo())
      .followUpDate(request.followUpDate())
      .build();

    Lead saved = leadService.updateLead(id, updated);
    return ResponseEntity.ok(ApiResponse.success(LeadResponse.fromEntity(saved), "Lead updated successfully"));
  }

  @PatchMapping("/{id}/status")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Update lead status")
  public ResponseEntity<ApiResponse<LeadResponse>> updateLeadStatus(
      @PathVariable UUID id,
      @Valid @RequestBody UpdateLeadStatusRequest request) {

    getLeadForCurrentTenant(id);

    Lead saved = leadService.updateLeadStatus(id, parseStatus(request.status()));
    return ResponseEntity.ok(ApiResponse.success(LeadResponse.fromEntity(saved), "Lead status updated"));
  }

  @PostMapping("/{id}/convert")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
  @Operation(summary = "Convert lead to member")
  public ResponseEntity<ApiResponse<LeadResponse>> convertLead(
      @PathVariable UUID id,
      @RequestBody(required = false) ConvertLeadRequest request) {

    getLeadForCurrentTenant(id);

    UUID memberId = request != null ? request.memberId() : null;
    Lead converted = leadService.convertLead(id, memberId);
    return ResponseEntity.ok(ApiResponse.success(LeadResponse.fromEntity(converted), "Lead converted successfully"));
  }

  @DeleteMapping("/{id}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
  @Operation(summary = "Delete lead")
  public ResponseEntity<ApiResponse<Void>> deleteLead(@PathVariable UUID id) {
    getLeadForCurrentTenant(id);

    leadService.deleteLead(id);
    return ResponseEntity.ok(ApiResponse.success(null, "Lead deleted successfully"));
  }

  // ===== HELPER METHODS =====

  /**
   * Loads a lead and verifies it belongs to the caller's organisation,
   * so tenants can never access another organisation's leads by guessing IDs.
   */
  private Lead getLeadForCurrentTenant(UUID id) {
    Lead lead = leadService.getLeadById(id);
    UUID organisationId = TenantContext.getCurrentTenantId();
    if (organisationId != null && lead.getOrganisationId() != null
        && !organisationId.equals(lead.getOrganisationId())) {
      // Respond as not-found so cross-tenant probing can't confirm a lead exists
      throw new ResourceNotFoundException("Lead", id.toString());
    }
    return lead;
  }

  private LeadStatus parseStatus(String status) {
    try {
      return LeadStatus.valueOf(status.trim().toUpperCase());
    } catch (IllegalArgumentException e) {
      throw new DomainException("INVALID_LEAD_STATUS",
        "Invalid lead status: " + status);
    }
  }

  private List<LeadResponse> toResponses(List<Lead> leads) {
    return leads.stream()
      .map(LeadResponse::fromEntity)
      .collect(Collectors.toList());
  }
}
