package com.gymmate.user.api;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.security.service.JwtService;
import com.gymmate.user.api.dto.InviteRequest;
import com.gymmate.user.api.dto.InviteResponse;
import com.gymmate.user.application.InviteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/gyms/{gymId}/invites")
@RequiredArgsConstructor
@Tag(name = "Invites", description = "Invite management APIs")
public class InviteController {

    private final InviteService inviteService;
    private final JwtService jwtService;

    @PostMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Create invite", description = "Invite a new user (Trainer or Staff)")
    public ResponseEntity<ApiResponse<InviteResponse>> createInvite(
            @PathVariable UUID gymId,
            @Valid @RequestBody InviteRequest request,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = jwtService.extractUserId(authHeader.substring(7));
        InviteResponse response = inviteService.createInvite(gymId, request, userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Invite sent successfully"));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get invites", description = "Get all invites for a gym")
    public ResponseEntity<ApiResponse<List<InviteResponse>>> getInvites(
            @PathVariable UUID gymId) {
        List<InviteResponse> responses = inviteService.getInvitesForGym(gymId);
        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @PostMapping("/{inviteId}/resend")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Resend invite", description = "Resend an expired or pending invite")
    public ResponseEntity<ApiResponse<InviteResponse>> resendInvite(
            @PathVariable UUID gymId,
            @PathVariable UUID inviteId,
            @RequestHeader("Authorization") String authHeader) {

        UUID userId = jwtService.extractUserId(authHeader.substring(7));
        InviteResponse response = inviteService.resendInvite(inviteId, userId);
        return ResponseEntity.ok(ApiResponse.success(response, "Invite resent successfully"));
    }

    @DeleteMapping("/{inviteId}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Revoke invite", description = "Revoke a pending invite")
    public ResponseEntity<ApiResponse<Void>> revokeInvite(
            @PathVariable UUID gymId,
            @PathVariable UUID inviteId) {
        inviteService.revokeInvite(inviteId);
        return ResponseEntity.ok(ApiResponse.success(null, "Invite revoked successfully"));
    }
}
