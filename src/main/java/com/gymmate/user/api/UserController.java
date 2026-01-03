package com.gymmate.user.api;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.security.AuthenticationService;
import com.gymmate.user.api.dto.UserProfileUpdateRequest;
import com.gymmate.user.api.dto.UserRegistrationRequest;
import com.gymmate.user.api.dto.UserResponse;
import com.gymmate.user.application.UserService;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRole;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "User", description = "User management operations")
public class UserController {
    private final UserService userService;

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
    * Get all users.
   */
    @GetMapping
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers() {
        List<User> users = userService.findAll();
        List<UserResponse> responses = users.stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

  /**
     * Update user profile.
     */
    @PutMapping("/{id}/profile")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UserProfileUpdateRequest request) {

        User user = userService.updateProfile(id, request.firstName(),
                request.lastName(), request.phone());

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
     * Get all gym admins.
     */
    @GetMapping("/gym-admins")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getGymAdmins() {
        List<User> gymAdmins = userService.findActiveGymAdmins();
        List<UserResponse> responses = gymAdmins.stream()
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
