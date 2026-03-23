package com.gymmate.user.api;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.security.TenantAwareUserDetails;
import com.gymmate.user.api.dto.UserProfileUpdateRequest;
import com.gymmate.user.api.dto.UserResponse;
import com.gymmate.user.application.UserService;
import com.gymmate.user.domain.User;
import com.gymmate.shared.constants.UserRole;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
     * SECURITY: Validates the fetched user belongs to the caller's organisation.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('GYM_OWNER', 'OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> getUserById(
            @PathVariable UUID id,
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        User user = userService.findById(id);

        // Tenant isolation: non-SUPER_ADMIN can only see users in their own org
        UUID callerOrgId = userDetails.getOrganisationId();
        if (callerOrgId != null && !callerOrgId.equals(user.getOrganisationId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You do not have permission to access this user"));
        }

        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get User profile by getting the user's id from the login token.
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UserResponse>> getUserProfile(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        User user = userService.findById(userDetails.getUserId());
        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get all users within the caller's organisation.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('GYM_OWNER', 'OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getAllUsers(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        List<User> users = userService.findByOrganisationId(userDetails.getOrganisationId());
        List<UserResponse> responses = users.stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Update user profile.
     * Users can only update their own profile. Admins can update profiles within their org.
     */
    @PutMapping("/{id}/profile")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<ApiResponse<UserResponse>> updateProfile(
            @PathVariable UUID id,
            @Valid @RequestBody UserProfileUpdateRequest request,
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {

        // Ownership check: user can only update their own profile, unless admin
        boolean isSelf = userDetails.getUserId().equals(id);
        boolean isAdmin = userDetails.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_OWNER")
                        || a.getAuthority().equals("ROLE_ADMIN")
                        || a.getAuthority().equals("ROLE_SUPER_ADMIN"));

        if (!isSelf && !isAdmin) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You can only update your own profile"));
        }

        // Tenant check for admin editing another user
        if (!isSelf && isAdmin) {
            User target = userService.findById(id);
            UUID callerOrgId = userDetails.getOrganisationId();
            if (callerOrgId != null && !callerOrgId.equals(target.getOrganisationId())) {
                return ResponseEntity.status(403)
                        .body(ApiResponse.error("You do not have permission to update this user"));
            }
        }

        User user = userService.updateProfile(id, request.firstName(),
                request.lastName(), request.phone());
        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }

    /**
     * Get all users by role within the caller's organisation.
     */
    @GetMapping("/by-role/{role}")
    @PreAuthorize("hasAnyRole('GYM_OWNER', 'OWNER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get users by role", description = "Get users by role scoped to organisation")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getUsersByRole(
            @PathVariable UserRole role,
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        UUID organisationId = userDetails.getOrganisationId();
        List<User> users = userService.findByRoleAndOrganisation(role, organisationId);
        List<UserResponse> responses = users.stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Get all gym admins within the caller's organisation.
     */
    @GetMapping("/gym-admins")
    @PreAuthorize("hasAnyRole('GYM_OWNER', 'OWNER', 'ADMIN', 'SUPER_ADMIN')")
    @Operation(summary = "Get gym admins", description = "Get active gym admins scoped to organisation")
    public ResponseEntity<ApiResponse<List<UserResponse>>> getGymAdmins(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        UUID organisationId = userDetails.getOrganisationId();
        List<User> gymAdmins = userService.findActiveGymAdmins(organisationId);
        List<UserResponse> responses = gymAdmins.stream()
                .map(UserResponse::fromEntity)
                .toList();
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    /**
     * Deactivate user account.
     * SECURITY: Validates user belongs to caller's organisation.
     */
    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasAnyRole('GYM_OWNER', 'OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> deactivateUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        UUID callerOrgId = userDetails.getOrganisationId();
        User target = userService.findById(id);
        if (callerOrgId != null && !callerOrgId.equals(target.getOrganisationId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You do not have permission to deactivate this user"));
        }
        User user = userService.deactivateUser(id);
        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(ApiResponse.success(response, "User deactivated successfully"));
    }

    /**
     * Activate user account.
     * SECURITY: Validates user belongs to caller's organisation.
     */
    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasAnyRole('GYM_OWNER', 'OWNER', 'ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<ApiResponse<UserResponse>> activateUser(
            @PathVariable UUID id,
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        UUID callerOrgId = userDetails.getOrganisationId();
        User target = userService.findById(id);
        if (callerOrgId != null && !callerOrgId.equals(target.getOrganisationId())) {
            return ResponseEntity.status(403)
                    .body(ApiResponse.error("You do not have permission to activate this user"));
        }
        User user = userService.activateUser(id);
        UserResponse response = UserResponse.fromEntity(user);
        return ResponseEntity.ok(ApiResponse.success(response, "User activated successfully"));
    }
}
