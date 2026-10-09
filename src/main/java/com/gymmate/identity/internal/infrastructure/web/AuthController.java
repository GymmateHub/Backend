package com.gymmate.identity.internal.infrastructure.web;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.security.TenantAwareUserDetails;
import com.gymmate.identity.internal.application.dto.ChangePasswordRequest;
import com.gymmate.identity.internal.application.dto.LoginRequest;
import com.gymmate.identity.internal.application.dto.LoginResponse;
import com.gymmate.identity.internal.application.dto.PasswordResetConfirmRequest;
import com.gymmate.identity.internal.application.dto.PasswordResetRequest;
import com.gymmate.identity.internal.application.dto.RefreshTokenRequest;
import com.gymmate.identity.internal.application.dto.RegistrationResponse;
import com.gymmate.identity.internal.application.dto.ResendOtpRequest;
import com.gymmate.identity.internal.application.dto.TokenResponse;
import com.gymmate.identity.internal.application.dto.VerificationTokenResponse;
import com.gymmate.identity.internal.application.dto.VerifyOtpRequest;
import com.gymmate.identity.internal.application.AuthenticationService;
import com.gymmate.identity.internal.application.dto.InviteAcceptRequest;
import com.gymmate.identity.internal.application.dto.ValidateInviteResponse;
import com.gymmate.identity.internal.application.InviteService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Authentication REST Controller.
 * Handles user registration, login, logout, password reset, and token
 * management.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization APIs")
public class AuthController {

        private final AuthenticationService authenticationService;
        private final InviteService inviteService;

        // ==================== INVITES ====================

        @GetMapping("/invite/validate")
        public ResponseEntity<ApiResponse<ValidateInviteResponse>> validateInvite(
                        @RequestParam String token) {
                ValidateInviteResponse response = inviteService.validateInvite(token);
                return ResponseEntity.ok(ApiResponse.success(response, "Invite validated"));
        }

        @PostMapping("/invite/accept")
        public ResponseEntity<ApiResponse<LoginResponse>> acceptInvite(
                        @Valid @RequestBody InviteAcceptRequest request) {
                LoginResponse response = authenticationService.acceptInvite(request);
                return ResponseEntity.ok(ApiResponse.success(response, "Invite accepted and user activated"));
        }

        // ==================== OTP VERIFICATION ====================

        @PostMapping("/register/resend-otp")
        public ResponseEntity<ApiResponse<RegistrationResponse>> resendOtp(
                        @Valid @RequestBody ResendOtpRequest request) {
                RegistrationResponse response = authenticationService.resendOtp(request);
                return ResponseEntity.ok(ApiResponse.success(response, response.message()));
        }

        @PostMapping("/register/verify-otp")
        public ResponseEntity<ApiResponse<VerificationTokenResponse>> verifyOtp(
                        @Valid @RequestBody VerifyOtpRequest request) {
                VerificationTokenResponse response = authenticationService.verifyOtp(request);
                return ResponseEntity.ok(ApiResponse.success(response, response.message()));
        }

        // ==================== LOGIN / LOGOUT ====================

        @PostMapping("/login")
        public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
                LoginResponse response = authenticationService.authenticate(request);
                String message = response.emailVerified()
                                ? "Login successful"
                                : "Email not verified. An OTP has been sent to your email.";
                return ResponseEntity.ok(ApiResponse.success(response, message));
        }

        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(
                        @RequestHeader(value = "Authorization", required = false) String bearerToken) {
                if (bearerToken == null || bearerToken.isBlank()) {
                        throw new DomainException("MISSING_TOKEN", "Authorization header is required for logout");
                }
                if (!bearerToken.startsWith("Bearer ")) {
                        throw new DomainException("INVALID_TOKEN_FORMAT",
                                        "Authorization header must start with 'Bearer '");
                }
                authenticationService.logout(bearerToken.substring(7));
                return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
        }

        // ==================== PASSWORD RESET ====================

        @PostMapping("/password-reset/request")
        public ResponseEntity<ApiResponse<Void>> requestPasswordReset(
                        @Valid @RequestBody PasswordResetRequest request) {
                authenticationService.initiatePasswordReset(request);
                return ResponseEntity.ok(ApiResponse.success(null, "Password reset email sent"));
        }

        @PostMapping("/password-reset/confirm")
        public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(
                        @Valid @RequestBody PasswordResetConfirmRequest request) {
                authenticationService.confirmPasswordReset(request);
                return ResponseEntity.ok(ApiResponse.success(null, "Password reset successful"));
        }

        // ==================== PASSWORD CHANGE ====================

        @PostMapping("/change-password")
        @Operation(summary = "Change password", description = "Change the authenticated user's password")
        public ResponseEntity<ApiResponse<Void>> changePassword(
                        @AuthenticationPrincipal TenantAwareUserDetails userDetails,
                        @Valid @RequestBody ChangePasswordRequest request) {
                authenticationService.changePassword(
                                userDetails.getUserId(), request.currentPassword(), request.newPassword());
                return ResponseEntity.ok(ApiResponse.success(null, "Password changed successfully"));
        }

        // ==================== TOKEN MANAGEMENT ====================

        @PostMapping("/refresh")
        public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
                        @Valid @RequestBody RefreshTokenRequest request) {
                TokenResponse response = authenticationService.refreshToken(request);
                return ResponseEntity.ok(ApiResponse.success(response, "Tokens refreshed successfully"));
        }
}
