package com.gymmate.lead.internal.web;

import com.gymmate.lead.api.dto.*;
import com.gymmate.lead.application.LeadService;
import com.gymmate.lead.domain.Lead;
import com.gymmate.lead.domain.LeadStatus;
import com.gymmate.shared.dto.ApiResponse;
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

@Slf4j
@RestController
@RequestMapping("/api/leads")
@RequiredArgsConstructor
@Tag(name = "Leads", description = "Lead management APIs")
public class LeadController {

    private final LeadService leadService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Create lead")
    public ResponseEntity<ApiResponse<LeadResponse>> createLead(@Valid @RequestBody LeadCreateRequest request) {
        UUID gymId = request.gymId();
        Lead lead = leadService.createLead(gymId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(LeadResponse.fromEntity(lead), "Lead created successfully"));
    }

    @GetMapping("/gym/{gymId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get leads by gym")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getByGym(@PathVariable UUID gymId) {
        List<LeadResponse> list = leadService.findByGymId(gymId).stream()
                .map(LeadResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/gym/{gymId}/status/{status}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get leads by gym and status")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getByGymAndStatus(
            @PathVariable UUID gymId,
            @PathVariable LeadStatus status) {
        List<LeadResponse> list = leadService.findByGymIdAndStatus(gymId, status).stream()
                .map(LeadResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/organisation")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get leads for organisation")
    public ResponseEntity<ApiResponse<List<LeadResponse>>> getByOrganisation() {
        UUID orgId = TenantContext.requireCurrentTenantId();
        List<LeadResponse> list = leadService.findByOrganisationId(orgId).stream()
                .map(LeadResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(list));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<LeadResponse>> getById(@PathVariable UUID id) {
        Lead lead = leadService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(LeadResponse.fromEntity(lead)));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<LeadResponse>> update(
            @PathVariable UUID id,
            @RequestBody LeadUpdateRequest request) {
        Lead lead = leadService.updateLead(id, request);
        return ResponseEntity.ok(ApiResponse.success(LeadResponse.fromEntity(lead), "Lead updated"));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<LeadResponse>> updateStatus(
            @PathVariable UUID id,
            @Valid @RequestBody LeadStatusUpdateRequest request) {
        LeadStatus status = LeadStatus.valueOf(request.status().toUpperCase());
        Lead lead = leadService.updateStatus(id, status);
        return ResponseEntity.ok(ApiResponse.success(LeadResponse.fromEntity(lead), "Status updated"));
    }

    @PostMapping("/{id}/convert")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<LeadResponse>> convert(
            @PathVariable UUID id,
            @RequestBody(required = false) LeadConvertRequest request) {
        UUID memberId = request != null ? request.memberId() : null;
        Lead lead = leadService.convert(id, memberId);
        return ResponseEntity.ok(ApiResponse.success(LeadResponse.fromEntity(lead), "Lead converted"));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    public ResponseEntity<ApiResponse<Void>> delete(@PathVariable UUID id) {
        leadService.deleteLead(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Lead deleted"));
    }
}
