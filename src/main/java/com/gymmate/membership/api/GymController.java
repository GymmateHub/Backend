package com.gymmate.membership.api;

import com.gymmate.membership.api.dto.GymRegistrationRequest;
import com.gymmate.membership.api.dto.GymResponse;
import com.gymmate.membership.application.GymService;
import com.gymmate.membership.domain.Gym;
import com.gymmate.shared.dto.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
                request.getOwnerId(),
                request.getName(),
                request.getDescription(),
                request.getStreet(),
                request.getCity(),
                request.getState(),
                request.getPostalCode(),
                request.getCountry(),
                request.getContactEmail(),
                request.getContactPhone()
        );
        
        GymResponse response = GymResponse.fromEntity(gym);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Gym registered successfully"));
    }
    
    /**
     * Get gym by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<GymResponse>> getGymById(@PathVariable Long id) {
        Gym gym = gymService.findById(id);
        GymResponse response = GymResponse.fromEntity(gym);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
    
    /**
     * Update gym details.
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<GymResponse>> updateGym(
            @PathVariable Long id,
            @Valid @RequestBody GymRegistrationRequest request) {
        
        Gym gym = gymService.updateGymDetails(
                id,
                request.getName(),
                request.getDescription(),
                request.getStreet(),
                request.getCity(),
                request.getState(),
                request.getPostalCode(),
                request.getCountry(),
                request.getContactEmail(),
                request.getContactPhone()
        );
        
        GymResponse response = GymResponse.fromEntity(gym);
        return ResponseEntity.ok(ApiResponse.success(response, "Gym updated successfully"));
    }
    
    /**
     * Get all gyms owned by a specific user.
     */
    @GetMapping("/owner/{ownerId}")
    public ResponseEntity<ApiResponse<List<GymResponse>>> getGymsByOwner(@PathVariable Long ownerId) {
        List<Gym> gyms = gymService.findByOwnerId(ownerId);
        List<GymResponse> responses = gyms.stream()
                .map(GymResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
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
    public ResponseEntity<ApiResponse<GymResponse>> activateGym(@PathVariable Long id) {
        Gym gym = gymService.activateGym(id);
        GymResponse response = GymResponse.fromEntity(gym);
        return ResponseEntity.ok(ApiResponse.success(response, "Gym activated successfully"));
    }
    
    /**
     * Deactivate a gym.
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<GymResponse>> deactivateGym(@PathVariable Long id) {
        Gym gym = gymService.deactivateGym(id);
        GymResponse response = GymResponse.fromEntity(gym);
        return ResponseEntity.ok(ApiResponse.success(response, "Gym deactivated successfully"));
    }
    
    /**
     * Suspend a gym.
     */
    @PatchMapping("/{id}/suspend")
    public ResponseEntity<ApiResponse<GymResponse>> suspendGym(@PathVariable Long id) {
        Gym gym = gymService.suspendGym(id);
        GymResponse response = GymResponse.fromEntity(gym);
        return ResponseEntity.ok(ApiResponse.success(response, "Gym suspended successfully"));
    }
}