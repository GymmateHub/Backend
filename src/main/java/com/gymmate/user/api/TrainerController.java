package com.gymmate.user.api;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.security.TenantAwareUserDetails;
import com.gymmate.user.api.dto.TrainerCreateRequest;
import com.gymmate.user.api.dto.TrainerResponse;
import com.gymmate.user.api.dto.TrainerUpdateRequest;
import com.gymmate.user.application.TrainerService;
import com.gymmate.user.domain.Trainer;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for trainer management operations.
 */
@RestController
@RequestMapping("/api/trainers")
@RequiredArgsConstructor
@Tag(name = "Trainer", description = "Trainer management operations")
public class TrainerController {

    private final TrainerService trainerService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Create trainer", description = "Create a new trainer profile")
    public ResponseEntity<ApiResponse<TrainerResponse>> createTrainer(@Valid @RequestBody TrainerCreateRequest request) {
        Trainer trainer = trainerService.createTrainer(
                request.userId(),
                request.specializations(),
                request.bio(),
                request.hourlyRate(),
                request.commissionRate(),
                request.hireDate(),
                request.employmentType()
        );
        TrainerResponse response = TrainerResponse.fromEntity(trainer);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Trainer created successfully"));
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
    public ResponseEntity<ApiResponse<TrainerResponse>> getTrainerById(@PathVariable UUID id) {
        Trainer trainer = trainerService.findById(id);
        // Tenant isolation: findById bypasses Hibernate @Filter
        UUID tenantId = TenantContext.getCurrentTenantId();
        if (tenantId != null && trainer.getOrganisationId() != null
                && !tenantId.equals(trainer.getOrganisationId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You do not have permission to access this trainer"));
        }
        TrainerResponse response = TrainerResponse.fromEntity(trainer);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    public ResponseEntity<ApiResponse<TrainerResponse>> getTrainerByUserId(@PathVariable UUID userId) {
        Trainer trainer = trainerService.findByUserId(userId);
        // Tenant isolation: findByUserId may bypass Hibernate @Filter
        UUID tenantId = TenantContext.getCurrentTenantId();
        if (tenantId != null && trainer.getOrganisationId() != null
                && !tenantId.equals(trainer.getOrganisationId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You do not have permission to access this trainer"));
        }
        TrainerResponse response = TrainerResponse.fromEntity(trainer);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF')")
    @Operation(summary = "Get all trainers", description = "Get all trainers in the current organisation")
    public ResponseEntity<ApiResponse<List<TrainerResponse>>> getAllTrainers(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        UUID organisationId = userDetails.getOrganisationId();
        List<Trainer> trainers = trainerService.findAllByOrganisation(organisationId);
        List<TrainerResponse> responses = trainers.stream()
                .map(TrainerResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'MEMBER')")
    @Operation(summary = "Get available trainers", description = "Get active trainers accepting clients in the current organisation")
    public ResponseEntity<ApiResponse<List<TrainerResponse>>> getAvailableTrainers(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        UUID organisationId = userDetails.getOrganisationId();
        List<Trainer> trainers = trainerService.findActiveAndAcceptingClients(organisationId);
        List<TrainerResponse> responses = trainers.stream()
                .map(TrainerResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PutMapping("/{id}/rate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TrainerResponse>> updateRate(
            @PathVariable UUID id,
            @Valid @RequestBody TrainerUpdateRequest request) {
        Trainer trainer = trainerService.updateRate(id, request.hourlyRate(), request.commissionRate());
        TrainerResponse response = TrainerResponse.fromEntity(trainer);
        return ResponseEntity.ok(ApiResponse.success(response, "Rate updated successfully"));
    }

    @PatchMapping("/{id}/toggle-accepting")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'TRAINER')")
    public ResponseEntity<ApiResponse<TrainerResponse>> toggleAcceptingClients(@PathVariable UUID id) {
        Trainer trainer = trainerService.toggleAcceptingClients(id);
        TrainerResponse response = TrainerResponse.fromEntity(trainer);
        return ResponseEntity.ok(ApiResponse.success(response, "Accepting clients status toggled"));
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TrainerResponse>> activate(@PathVariable UUID id) {
        Trainer trainer = trainerService.activate(id);
        TrainerResponse response = TrainerResponse.fromEntity(trainer);
        return ResponseEntity.ok(ApiResponse.success(response, "Trainer activated successfully"));
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    public ResponseEntity<ApiResponse<TrainerResponse>> deactivate(@PathVariable UUID id) {
        Trainer trainer = trainerService.deactivate(id);
        TrainerResponse response = TrainerResponse.fromEntity(trainer);
        return ResponseEntity.ok(ApiResponse.success(response, "Trainer deactivated successfully"));
    }
}
