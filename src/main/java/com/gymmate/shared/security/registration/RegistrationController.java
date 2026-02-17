package com.gymmate.shared.security.registration;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.security.dto.MemberRegistrationRequest;
import com.gymmate.shared.security.dto.MemberRegistrationResponse;
import com.gymmate.shared.security.dto.OwnerRegistrationRequest;
import com.gymmate.shared.security.dto.OwnerRegistrationResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * Controller for self-registration endpoints.
 *
 * Public endpoints (no auth required):
 * - POST /api/auth/register/owner  - Gym owner self-registration
 * - POST /api/auth/register/member - Member self-registration (via gym slug)
 *
 * Both flows require OTP verification after initial registration.
 * Use /api/auth/register/verify-otp to complete registration.
 *
 * For ADMIN, TRAINER, STAFF roles - use the invite flow:
 * - GET  /api/auth/invite/validate
 * - POST /api/auth/invite/accept
 */
@RestController
@RequestMapping("/api/auth/register")
@RequiredArgsConstructor
@Tag(name = "Registration", description = "Self-registration endpoints for owners and members")
public class RegistrationController {

    private final OwnerRegistrationService ownerRegistrationService;
    private final MemberRegistrationService memberRegistrationService;

    /**
     * Register a new gym owner.
     * Creates organisation, user, and default gym.
     * Sends OTP email for verification.
     */
    @PostMapping("/owner")
    @Operation(
        summary = "Register as gym owner",
        description = "Self-register as a gym owner. Creates your organisation, user account, and first gym. " +
                      "An OTP will be sent to your email for verification."
    )
    public ResponseEntity<ApiResponse<OwnerRegistrationResponse>> registerOwner(
            @Valid @RequestBody OwnerRegistrationRequest request) {
        OwnerRegistrationResponse response = ownerRegistrationService.registerOwner(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, response.message()));
    }

    /**
     * Register as a gym member.
     * Uses gym slug to identify the gym.
     * Sends OTP email for verification.
     */
    @PostMapping("/member")
    @Operation(
        summary = "Register as gym member",
        description = "Self-register as a member at a specific gym using the gym's slug. " +
                      "An OTP will be sent to your email for verification."
    )
    public ResponseEntity<ApiResponse<MemberRegistrationResponse>> registerMember(
            @Valid @RequestBody MemberRegistrationRequest request) {
        MemberRegistrationResponse response = memberRegistrationService.registerMember(request);
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response, response.message()));
    }
}

