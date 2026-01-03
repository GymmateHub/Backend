package com.gymmate.gym.api;

import com.gymmate.gym.api.dto.*;
import com.gymmate.gym.application.GymService;
import com.gymmate.gym.domain.Gym;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.security.JwtService;
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
 * REST controller for gym management operations.
 */
@Slf4j
@RestController
@RequestMapping("/api/gyms")
@RequiredArgsConstructor
@Tag(name = "Gyms", description = "Gym Management APIs")
public class GymController {

    private final GymService gymService;
    private final JwtService jwtService;

    /**
     * Register a new gym.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<GymResponse>> registerGym(@Valid @RequestBody GymRegistrationRequest request) {
        Gym gym = gymService.registerGym(request);
        GymResponse response = GymResponse.fromEntity(gym);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Gym registered successfully"));
    }

    /**
     * Update gym address.
     */
    @PutMapping("/{id}/address")
    public ResponseEntity<ApiResponse<GymResponse>> updateGymAddress(
            @PathVariable UUID id,
            @Valid @RequestBody AddressUpdateRequest request) {

        Gym gym = gymService.updateGymAddress(
                id,
                request.street(),
                request.city(),
                request.state(),
                request.postalCode(),
                request.country()
        );

        return ResponseEntity.ok(ApiResponse.success(
                GymResponse.fromEntity(gym),
                "Gym address updated successfully"
        ));
    }

    /**
     * Update gym details.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GymResponse>> updateGym(
            @PathVariable UUID id,
            @Valid @RequestBody GymUpdateRequest request) {

        Gym gym = gymService.updateGymDetails(id, request);

        return ResponseEntity.ok(ApiResponse.success(
                GymResponse.fromEntity(gym),
                "Gym details updated successfully"
        ));
    }

    /**
     * Get gym by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GymResponse>> getGym(@PathVariable UUID id) {
        Gym gym = gymService.getGymById(id);
        return ResponseEntity.ok(ApiResponse.success(GymResponse.fromEntity(gym)));
    }

    /**
     * Get all gyms in the authenticated user's organisation.
     * This is the preferred method for getting gyms.
     */
    @GetMapping("/my-gyms")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'SUPER_ADMIN')")
    @Operation(summary = "Get my gyms", description = "Get all gyms in the authenticated user's organisation")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getMyGyms(
            @RequestHeader("Authorization") String authHeader) {

        // Get organisation ID from tenant context (preferred) or JWT
        UUID organisationId = TenantContext.getCurrentTenantId();

        if (organisationId == null) {
            // Fallback to extracting from JWT
            String token = authHeader.substring(7);
            organisationId = jwtService.extractOrganisationId(token);
        }

        if (organisationId == null) {
            // Legacy fallback - use deprecated owner-based query
            log.warn("No organisation context found, falling back to owner-based query (deprecated)");
            String token = authHeader.substring(7);
            UUID userId = jwtService.extractUserId(token);
            List<GymResponse> gyms = gymService.getGymsByOwner(userId).stream()
                    .map(GymResponse::fromEntity)
                    .toList();
            return ResponseEntity.ok(ApiResponse.success(gyms));
        }

        List<GymResponse> gyms = gymService.getGymsByOrganisation(organisationId).stream()
                .map(GymResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(gyms));
    }

    /**
     * Get all gyms owned by a specific user (ADMIN/SUPER_ADMIN only).
     * @deprecated Use /api/organisations/current/gyms instead
     */
    @Deprecated(since = "1.0", forRemoval = true)
    @GetMapping("/owner/{ownerId}")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get gyms by owner (deprecated)", description = "Deprecated: Use /api/organisations/current/gyms instead")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getGymsByOwner(@PathVariable UUID ownerId) {
        log.warn("Deprecated endpoint /owner/{} called - use /api/organisations/current/gyms instead", ownerId);
        List<GymResponse> gyms = gymService.getGymsByOwner(ownerId).stream()
                .map(GymResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(gyms));
    }

    /**
     * Get all active gyms.
     */
    @GetMapping("/active")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getActiveGyms() {
        List<Gym> gyms = gymService.findActiveGyms();
        List<GymResponse> responses = gyms.stream()
                .map(GymResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get gyms by city.
     */
    @GetMapping("/city/{city}")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getGymsByCity(@PathVariable String city) {
        List<Gym> gyms = gymService.findByCity(city);
        List<GymResponse> responses = gyms.stream()
                .map(GymResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get all gyms (SUPER_ADMIN only).
     * Regular owners should use /my-gyms endpoint.
     */
    @GetMapping
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getAllGyms() {
        List<Gym> gyms = gymService.findAll();
        List<GymResponse> responses = gyms.stream()
                .map(GymResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Activate a gym.
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<GymResponse>> activateGym(@PathVariable UUID id) {
        Gym gym = gymService.activateGym(id);
        return ResponseEntity.ok(ApiResponse.success(GymResponse.fromEntity(gym), "Gym activated successfully"));
    }

    /**
     * Deactivate a gym.
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<GymResponse>> deactivateGym(@PathVariable UUID id) {
        Gym gym = gymService.deactivateGym(id);
        return ResponseEntity.ok(ApiResponse.success(GymResponse.fromEntity(gym), "Gym deactivated successfully"));
    }

    /**
     * Suspend a gym.
     */
    @PatchMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<GymResponse>> suspendGym(@PathVariable UUID id) {
        Gym gym = gymService.suspendGym(id);
        return ResponseEntity.ok(ApiResponse.success(GymResponse.fromEntity(gym), "Gym suspended successfully"));
    }

    /**
     * Delete a gym.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteGym(@PathVariable UUID id) {
        gymService.deleteGym(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Gym deleted successfully"));
    }

    /**
     * Complete gym onboarding.
     */
    @PatchMapping("/{id}/complete-onboarding")
    public ResponseEntity<ApiResponse<GymResponse>> completeOnboarding(@PathVariable UUID id) {
        Gym gym = gymService.completeOnboarding(id);
        return ResponseEntity.ok(ApiResponse.success(GymResponse.fromEntity(gym), "Onboarding completed successfully"));
    }

    /**
     * Update gym logo.
     */
    @PatchMapping("/{id}/logo")
    public ResponseEntity<ApiResponse<GymResponse>> updateLogo(
            @PathVariable UUID id,
            @RequestParam String logoUrl) {
        Gym gym = gymService.updateLogo(id, logoUrl);
        return ResponseEntity.ok(ApiResponse.success(GymResponse.fromEntity(gym), "Logo updated successfully"));
    }

    /**
     * Update gym website.
     */
    @PatchMapping("/{id}/website")
    public ResponseEntity<ApiResponse<GymResponse>> updateWebsite(
            @PathVariable UUID id,
            @RequestParam String website) {
        Gym gym = gymService.updateWebsite(id, website);
        return ResponseEntity.ok(ApiResponse.success(GymResponse.fromEntity(gym), "Website updated successfully"));
    }

    /**
     * Check if gym subscription is expired.
     */
    @GetMapping("/{id}/subscription/expired")
    public ResponseEntity<ApiResponse<Boolean>> isSubscriptionExpired(@PathVariable UUID id) {
        boolean expired = gymService.isSubscriptionExpired(id);
        return ResponseEntity.ok(ApiResponse.success(expired));
    }

    /**
     * Update gym subscription.
     */
    @PutMapping("/{id}/subscription")
    public ResponseEntity<ApiResponse<GymResponse>> updateSubscription(
            @PathVariable UUID id,
            @Valid @RequestBody SubscriptionUpdateRequest request) {
        Gym gym = gymService.updateSubscription(id, request.plan(), request.expiresAt());
        return ResponseEntity.ok(ApiResponse.success(GymResponse.fromEntity(gym), "Subscription updated successfully"));
    }

    /**
     * Update gym business settings.
     */
    @PutMapping("/{id}/business-settings")
    public ResponseEntity<ApiResponse<GymResponse>> updateBusinessSettings(
            @PathVariable UUID id,
            @Valid @RequestBody BusinessSettingsUpdateRequest request) {
        Gym gym = gymService.updateBusinessSettings(
                id,
                request.timezone(),
                request.currency(),
                request.businessHours()
        );

        if (request.maxMembers() != null) {
            gym = gymService.updateMaxMembers(id, request.maxMembers());
        }

        if (request.featuresEnabled() != null) {
            gym = gymService.updateFeatures(id, request.featuresEnabled());
        }

        return ResponseEntity.ok(ApiResponse.success(GymResponse.fromEntity(gym), "Business settings updated successfully"));
    }

    /**
     * Get aggregated analytics for all gyms owned by the authenticated user.
     * Extracts userId from JWT token to ensure security.
     */
    @GetMapping("/analytics")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<GymAnalyticsResponse>> getOwnerAnalytics(
            @RequestHeader("Authorization") String authHeader) {

        // Extract token from Authorization header
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        UUID userId = jwtService.extractUserId(token);

        GymAnalyticsResponse analytics = gymService.getOwnerAnalytics(userId);
        return ResponseEntity.ok(ApiResponse.success(analytics, "Analytics retrieved successfully"));
    }

    /**
     * Get analytics for a specific gym.
     * Validates that the authenticated user owns the gym before returning analytics.
     */
    @GetMapping("/{gymId}/analytics")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<GymAnalyticsResponse>> getGymAnalytics(
            @PathVariable UUID gymId,
            @RequestHeader("Authorization") String authHeader) {

        // Extract token from Authorization header
        String token = authHeader.substring(7); // Remove "Bearer " prefix
        UUID userId = jwtService.extractUserId(token);

        GymAnalyticsResponse analytics = gymService.getGymAnalytics(gymId, userId);
        return ResponseEntity.ok(ApiResponse.success(analytics, "Gym analytics retrieved successfully"));
    }
}
