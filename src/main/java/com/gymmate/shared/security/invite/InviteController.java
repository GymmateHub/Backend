package com.gymmate.shared.security.invite;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.security.invite.dto.InviteAcceptRequest;
import com.gymmate.shared.security.invite.dto.InviteAcceptResponse;
import com.gymmate.shared.security.invite.dto.InviteValidateResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * Controller for invite-related authentication endpoints.
 *
 * Public endpoints (no auth required):
 * - GET  /api/auth/invite/validate?token=xxx - Validate invite token
 * - POST /api/auth/invite/accept - Accept invite and create account
 *
 * The invite management endpoints (create, resend, revoke, list) are in GymController
 * under /api/gyms/{gymId}/invites since they require gym context and authorization.
 */
@RestController
@RequestMapping("/api/auth/invite")
@RequiredArgsConstructor
@Tag(name = "Invite Authentication", description = "Public endpoints for accepting invites")
public class InviteController {

    private final InviteService inviteService;

    /**
     * Validate an invite token and return context for the accept form.
     * Called when user clicks the invite link in their email.
     */
    @GetMapping("/validate")
    @Operation(
        summary = "Validate invite token",
        description = "Validates the invite token and returns context for the accept form including gym name, role, and pre-filled data"
    )
    public ResponseEntity<ApiResponse<InviteValidateResponse>> validateInvite(
            @RequestParam String token) {
        InviteValidateResponse response = inviteService.validateInvite(token);

        if (response.expired()) {
            return ResponseEntity.ok(ApiResponse.success(response,
                "This invite has expired. Please contact your administrator to resend it."));
        }

        return ResponseEntity.ok(ApiResponse.success(response, "Invite is valid"));
    }

    /**
     * Accept an invite - create user account and return JWT tokens.
     * The user is immediately logged in after accepting.
     */
    @PostMapping("/accept")
    @Operation(
        summary = "Accept invite",
        description = "Accept the invite by setting a password. Creates the user account and returns JWT tokens for immediate login."
    )
    public ResponseEntity<ApiResponse<InviteAcceptResponse>> acceptInvite(
            @Valid @RequestBody InviteAcceptRequest request) {
        InviteAcceptResponse response = inviteService.acceptInvite(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Account created successfully. You are now logged in."));
    }
}

