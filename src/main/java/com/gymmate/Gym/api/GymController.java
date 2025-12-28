package com.gymmate.Gym.api;

import com.gymmate.Gym.api.dto.*;
import com.gymmate.Gym.application.GymService;
import com.gymmate.Gym.domain.Gym;
import com.gymmate.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for gym management operations.
 */
@RestController
@RequestMapping("/api/gyms")
@RequiredArgsConstructor
public class GymController {

    private final GymService gymService;

    /**
     * Register a new gym.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<GymResponse>> registerGym(@Valid @RequestBody GymRegistrationRequest request) {
        Gym gym = gymService.registerGym(
                request.ownerId(),
                request.name(),
                request.description(),
                request.contactEmail(),
                request.contactPhone()
        );

        // If address details are provided, update the address
        if (request.street() != null && request.city() != null &&
                request.state() != null && request.postalCode() != null &&
                request.country() != null) {

            gym = gymService.updateGymAddress(
                    gym.getId(),
                    request.street(),
                    request.city(),
                    request.state(),
                    request.postalCode(),
                    request.country()
            );
        }

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
     * Update gym details without address.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GymResponse>> updateGym(
            @PathVariable UUID id,
            @Valid @RequestBody GymUpdateRequest request) {

        Gym gym = gymService.updateGymDetails(
                id,
                request.name(),
                request.description(),
                request.contactEmail(),
                request.contactPhone()
        );

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
     * Get all gyms owned by a specific user.
     */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getGymsByOwner(@PathVariable UUID ownerId) {
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
     * Get all gyms.
     */
    @GetMapping
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
}
