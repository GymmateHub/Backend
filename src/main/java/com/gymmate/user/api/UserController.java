package com.gymmate.user.api;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.user.api.dto.UserProfileUpdateRequest;
import com.gymmate.user.api.dto.UserRegistrationRequest;
import com.gymmate.user.api.dto.UserResponse;
import com.gymmate.user.application.UserRegistrationService;
import com.gymmate.user.application.UserService;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRole;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

/**
 * REST controller for user management operations.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserRegistrationService userRegistrationService;
    private final UserService userService;

    /**
     * Register a new user.
     */
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
        User user = userRegistrationService.registerUser(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword(),
                request.getPhoneNumber(),
                request.getRole()
        );

        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "User registered successfully"));
    }

    /**
     * Register a new gym member (convenience endpoint).
     */
    @PostMapping("/register/member")
    public ResponseEntity<ApiResponse<UserResponse>> registerMember(@Valid @RequestBody UserRegistrationRequest request) {
        User user = userRegistrationService.registerMember(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword(),
                request.getPhoneNumber()
        );

        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Member registered successfully"));
    }

    /**
     * Register a new gym owner (convenience endpoint).
     */
    @PostMapping("/register/gym-owner")
    public ResponseEntity<ApiResponse<UserResponse>> registerGymOwner(@Valid @RequestBody UserRegistrationRequest request) {
        User user = userRegistrationService.registerGymOwner(
                request.getEmail(),
                request.getFirstName(),
                request.getLastName(),
                request.getPassword(),
                request.getPhoneNumber()
        );

        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Gym owner registered successfully"));
    }

    /**
     * Get user by ID.
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(@PathVariable UUID id) {
        User user = userService.findById(id);
        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Update user profile.
     */
    @PutMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UserProfileUpdateRequest request) {

        User user = userService.updateProfile(id, request.getFirstName(),
                request.getLastName(), request.getPhoneNumber());

        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }

    /**
     * Get all users by role.
     */
    @GetMapping("/by-role/{role}")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(@PathVariable UserRole role) {
        List<User> users = userService.findByRole(role);
        List<UserResponse> responses = users.stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get all gym owners.
     */
    @GetMapping("/gym-owners")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getGymOwners() {
        List<User> gymOwners = userService.findActiveGymOwners();
        List<UserResponse> responses = gymOwners.stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Deactivate user account.
     */
    @PatchMapping("/{id}/deactivate")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(@PathVariable UUID id) {
        User user = userService.deactivateUser(id);
        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(ApiResponse.success(response, "User deactivated successfully"));
    }

    /**
     * Activate user account.
     */
    @PatchMapping("/{id}/activate")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(@PathVariable UUID id) {
        User user = userService.activateUser(id);
        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(ApiResponse.success(response, "User activated successfully"));
    }
}
