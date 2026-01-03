package com.gymmate.shared.api;

import com.gymmate.gym.api.dto.GymResponse;
import com.gymmate.gym.application.GymService;
import com.gymmate.gym.domain.Gym;
import com.gymmate.shared.api.dto.*;
import com.gymmate.shared.domain.Organisation;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.service.OrganisationLimitService;
import com.gymmate.shared.service.OrganisationLimitService.OrganisationUsage;
import com.gymmate.shared.service.OrganisationService;
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

/**
 * REST controller for organisation management operations.
 * Provides endpoints for managing the current user's organisation,
 * including details, usage stats, and gyms.
 */
@Slf4j
@RestController
@RequestMapping("/api/organisations")
@RequiredArgsConstructor
@Tag(name = "Organisations", description = "Organisation Management APIs")
public class OrganisationController {

    private final OrganisationService organisationService;
    private final OrganisationLimitService limitService;
    private final GymService gymService;

    /**
     * Get the current organisation details.
     */
    @GetMapping("/current")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(summary = "Get current organisation", description = "Get details of the authenticated user's organisation")
    public ResponseEntity<ApiResponse<OrganisationResponse>> getCurrentOrganisation() {
        UUID organisationId = requireOrganisationId();

        Organisation organisation = organisationService.getById(organisationId);
        OrganisationResponse response = OrganisationResponse.fromEntity(organisation);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update the current organisation details.
     */
    @PutMapping("/current")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Update current organisation", description = "Update details of the authenticated user's organisation")
    public ResponseEntity<ApiResponse<OrganisationResponse>> updateCurrentOrganisation(
            @Valid @RequestBody OrganisationUpdateRequest request) {

        UUID organisationId = requireOrganisationId();

        Organisation organisation = organisationService.updateDetails(
            organisationId,
            request.getName(),
            request.getContactEmail(),
            request.getContactPhone(),
            request.getBillingEmail(),
            request.getSettings()
        );

        OrganisationResponse response = OrganisationResponse.fromEntity(organisation);
        return ResponseEntity.ok(ApiResponse.success(response, "Organisation updated successfully"));
    }

    /**
     * Get usage statistics for the current organisation.
     * Shows current resource usage vs subscription limits.
     */
    @GetMapping("/current/usage")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get organisation usage", description = "Get usage statistics (gyms, members, staff) vs subscription limits")
    public ResponseEntity<ApiResponse<OrganisationUsageResponse>> getOrganisationUsage() {
        UUID organisationId = requireOrganisationId();

        OrganisationUsage usage = limitService.getUsage(organisationId);
        OrganisationUsageResponse response = OrganisationUsageResponse.fromUsage(usage);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all gyms in the current organisation.
     */
    @GetMapping("/current/gyms")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(summary = "Get organisation gyms", description = "Get all gyms belonging to the current organisation")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getOrganisationGyms() {
        UUID organisationId = requireOrganisationId();

        List<GymResponse> gyms = gymService.getGymsByOrganisation(organisationId)
                .stream()
                .map(GymResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(gyms));
    }

    /**
     * Get only active gyms in the current organisation.
     */
    @GetMapping("/current/gyms/active")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
    @Operation(summary = "Get active organisation gyms", description = "Get all active gyms belonging to the current organisation")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getActiveOrganisationGyms() {
        UUID organisationId = requireOrganisationId();

        List<GymResponse> gyms = gymService.getActiveGymsByOrganisation(organisationId)
                .stream()
                .map(GymResponse::fromEntity)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(gyms));
    }

    /**
     * Create a new gym in the current organisation.
     * Checks subscription limits before creating.
     */
    @PostMapping("/current/gyms")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Create a new gym", description = "Create a new gym location within the current organisation")
    public ResponseEntity<ApiResponse<GymResponse>> createGym(
            @Valid @RequestBody CreateGymRequest request) {

        UUID organisationId = requireOrganisationId();

        // Check if organisation can create more gyms
        limitService.checkCanCreateGym(organisationId);

        // Create the gym
        Gym gym = new Gym(
            request.getName(),
            request.getDescription(),
            request.getContactEmail(),
            request.getContactPhone(),
            organisationId
        );

        // Set optional fields
        if (request.getAddress() != null) {
            gym.setAddress(request.getAddress());
        }
        if (request.getCity() != null) {
            gym.setCity(request.getCity());
        }
        if (request.getState() != null) {
            gym.setState(request.getState());
        }
        if (request.getCountry() != null) {
            gym.setCountry(request.getCountry());
        }
        if (request.getPostalCode() != null) {
            gym.setPostalCode(request.getPostalCode());
        }
        if (request.getTimezone() != null) {
            gym.setTimezone(request.getTimezone());
        }
        if (request.getCurrency() != null) {
            gym.setCurrency(request.getCurrency());
        }

        Gym savedGym = gymService.saveGym(gym);

        log.info("Created new gym '{}' (ID: {}) for organisation {}",
            savedGym.getName(), savedGym.getId(), organisationId);

        GymResponse response = GymResponse.fromEntity(savedGym);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Gym created successfully"));
    }

    /**
     * Complete onboarding for the current organisation.
     */
    @PostMapping("/current/complete-onboarding")
    @PreAuthorize("hasRole('OWNER')")
    @Operation(summary = "Complete onboarding", description = "Mark the organisation's onboarding as complete")
    public ResponseEntity<ApiResponse<OrganisationResponse>> completeOnboarding() {
        UUID organisationId = requireOrganisationId();

        Organisation organisation = organisationService.completeOnboarding(organisationId);
        OrganisationResponse response = OrganisationResponse.fromEntity(organisation);

        return ResponseEntity.ok(ApiResponse.success(response, "Onboarding completed successfully"));
    }

    /**
     * Helper method to get and validate organisation ID from tenant context.
     */
    private UUID requireOrganisationId() {
        UUID organisationId = TenantContext.getCurrentTenantId();
        if (organisationId == null) {
            throw new DomainException("NO_ORGANISATION_CONTEXT",
                "No organisation context found. Please ensure you are authenticated.");
        }
        return organisationId;
    }
}

