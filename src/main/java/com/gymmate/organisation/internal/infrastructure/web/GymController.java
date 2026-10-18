package com.gymmate.organisation.internal.infrastructure.web;

import com.gymmate.organisation.internal.application.dto.AddressUpdateRequest;
import com.gymmate.organisation.internal.application.dto.BusinessSettingsUpdateRequest;
import com.gymmate.organisation.internal.application.dto.GymAnalyticsResponse;
import com.gymmate.organisation.internal.application.dto.GymRegistrationRequest;
import com.gymmate.organisation.internal.application.dto.GymResponse;
import com.gymmate.organisation.internal.application.dto.GymUpdateRequest;
import com.gymmate.organisation.internal.application.dto.SubscriptionUpdateRequest;
import com.gymmate.organisation.internal.application.GymService;
import com.gymmate.organisation.internal.domain.Gym;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.security.CurrentUser;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.AccessDeniedException;
import com.gymmate.shared.security.TenantAwareUserDetails;
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

    /**
     * Register a new gym.
     */
    @PostMapping("/register")
    @PreAuthorize("hasAuthority('org:manage')")
    public ResponseEntity<ApiResponse<GymResponse>> registerGym(
            @Valid @RequestBody GymRegistrationRequest request,
            @AuthenticationPrincipal TenantAwareUserDetails caller) {
        // The gym is registered for the caller. Naming another owner is a platform-admin action;
        // this endpoint used to be public and trusted any ownerId in the body.
        UUID ownerId = caller.getUserId();
        if (request.ownerId() != null && !request.ownerId().equals(ownerId)) {
            if (!"SUPER_ADMIN".equals(caller.getRole())) {
                throw new AccessDeniedException("Only a platform admin can register a gym for another owner");
            }
            ownerId = request.ownerId();
        }
        Gym gym = gymService.registerGym(request, ownerId);
        GymResponse response = GymResponse.fromEntity(gym);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Gym registered successfully"));
    }

    /**
     * Update gym address.
     */
    @PutMapping("/{id}/address")
    @PreAuthorize("hasAuthority('org:manage')")
    public ResponseEntity<ApiResponse<GymResponse>> updateGymAddress(
            @PathVariable UUID id,
            @Valid @RequestBody AddressUpdateRequest request) {

        Gym gym = gymService.updateGymAddress(
                id,
                request.street(),
                request.city(),
                request.state(),
                request.postalCode(),
                request.country());

        return ResponseEntity.ok(ApiResponse.success(
                GymResponse.fromEntity(gym),
                "Gym address updated successfully"));
    }

    /**
     * Update gym details.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('org:manage')")
    public ResponseEntity<ApiResponse<GymResponse>> updateGym(
            @PathVariable UUID id,
            @Valid @RequestBody GymUpdateRequest request) {

        Gym gym = gymService.updateGymDetails(id, request);

        return ResponseEntity.ok(ApiResponse.success(
                GymResponse.fromEntity(gym),
                "Gym details updated successfully"));
    }

    /**
     * Get gym by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAuthority('org:view')")
    public ResponseEntity<ApiResponse<GymResponse>> getGym(@PathVariable UUID id) {
        Gym gym = gymService.getGymById(id);
        return ResponseEntity.ok(ApiResponse.success(GymResponse.fromEntity(gym)));
    }

    /**
     * Get all gyms in the authenticated user's organisation.
     * This is the preferred method for getting gyms.
     */
    @GetMapping("/my-gyms")
    @PreAuthorize("hasAuthority('org:view')")
    @Operation(summary = "Get my gyms", description = "Get all gyms in the authenticated user's organisation")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getMyGyms() {

        UUID organisationId = TenantContext.requireCurrentTenantId();

        List<GymResponse> gyms = gymService.getGymsByOrganisation(organisationId).stream()
                .map(GymResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(gyms));
    }

    /**
     * Get all active gyms.
     */
    @GetMapping("/active")
    @PreAuthorize("isAuthenticated()")
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
    @PreAuthorize("hasAuthority('platform:admin')")
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
    @PreAuthorize("hasAuthority('org:manage')")
    public ResponseEntity<ApiResponse<GymResponse>> activateGym(@PathVariable UUID id) {
        Gym gym = gymService.activateGym(id);
        return ResponseEntity.ok(ApiResponse.success(GymResponse.fromEntity(gym), "Gym activated successfully"));
    }

    /**
     * Deactivate a gym.
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAuthority('org:manage')")
    public ResponseEntity<ApiResponse<GymResponse>> deactivateGym(@PathVariable UUID id) {
        Gym gym = gymService.deactivateGym(id);
        return ResponseEntity.ok(ApiResponse.success(GymResponse.fromEntity(gym), "Gym deactivated successfully"));
    }

    /**
     * Suspend a gym.
     */
    @PatchMapping("/{id}/suspend")
    @PreAuthorize("hasAuthority('org:manage')")
    public ResponseEntity<ApiResponse<GymResponse>> suspendGym(@PathVariable UUID id) {
        Gym gym = gymService.suspendGym(id);
        return ResponseEntity.ok(ApiResponse.success(GymResponse.fromEntity(gym), "Gym suspended successfully"));
    }

    /**
     * Delete a gym.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('org:billing')")
    public ResponseEntity<ApiResponse<Void>> deleteGym(@PathVariable UUID id) {
        gymService.deleteGym(id);
        return ResponseEntity.ok(ApiResponse.success(null, "Gym deleted successfully"));
    }

    /**
     * Complete gym onboarding.
     */
    @PatchMapping("/{id}/complete-onboarding")
    @PreAuthorize("hasAuthority('org:manage')")
    public ResponseEntity<ApiResponse<GymResponse>> completeOnboarding(@PathVariable UUID id) {
        Gym gym = gymService.completeOnboarding(id);
        return ResponseEntity.ok(ApiResponse.success(GymResponse.fromEntity(gym), "Onboarding completed successfully"));
    }

    /**
     * Update gym logo.
     */
    @PatchMapping("/{id}/logo")
    @PreAuthorize("hasAuthority('org:manage')")
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
    @PreAuthorize("hasAuthority('org:manage')")
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
    @PreAuthorize("hasAuthority('org:billing')")
    public ResponseEntity<ApiResponse<Boolean>> isSubscriptionExpired(@PathVariable UUID id) {
        boolean expired = gymService.isSubscriptionExpired(id);
        return ResponseEntity.ok(ApiResponse.success(expired));
    }

    /**
     * Update gym subscription.
     */
    @PutMapping("/{id}/subscription")
    @PreAuthorize("hasAuthority('org:billing')")
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
    @PreAuthorize("hasAuthority('org:manage')")
    public ResponseEntity<ApiResponse<GymResponse>> updateBusinessSettings(
            @PathVariable UUID id,
            @Valid @RequestBody BusinessSettingsUpdateRequest request) {
        Gym gym = gymService.updateBusinessSettings(
                id,
                request.timezone(),
                request.currency(),
                request.businessHours());

        if (request.maxMembers() != null) {
            gym = gymService.updateMaxMembers(id, request.maxMembers());
        }

        if (request.featuresEnabled() != null) {
            gym = gymService.updateFeatures(id, request.featuresEnabled());
        }

        return ResponseEntity
                .ok(ApiResponse.success(GymResponse.fromEntity(gym), "Business settings updated successfully"));
    }

    /**
     * Get aggregated analytics for all gyms owned by the authenticated user.
     * Extracts userId from JWT token to ensure security.
     */
    @GetMapping("/analytics")
    @PreAuthorize("hasAuthority('analytics:view')")
    public ResponseEntity<ApiResponse<GymAnalyticsResponse>> getOwnerAnalytics(
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = CurrentUser.requireUserId();

        GymAnalyticsResponse analytics = gymService.getOwnerAnalytics(userId);
        return ResponseEntity.ok(ApiResponse.success(analytics, "Analytics retrieved successfully"));
    }

    /**
     * Get analytics for a specific gym.
     * Validates that the authenticated user owns the gym before returning
     * analytics.
     */
    @GetMapping("/{gymId}/analytics")
    @PreAuthorize("hasAuthority('analytics:view')")
    public ResponseEntity<ApiResponse<GymAnalyticsResponse>> getGymAnalytics(
            @PathVariable UUID gymId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = CurrentUser.requireUserId();

        GymAnalyticsResponse analytics = gymService.getGymAnalytics(gymId, userId);
        return ResponseEntity.ok(ApiResponse.success(analytics, "Gym analytics retrieved successfully"));
    }
}
