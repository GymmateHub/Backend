package com.gymmate.shared.security.api;

import com.gymmate.gym.application.GymService;
import com.gymmate.gym.domain.Gym;
import com.gymmate.notification.infrastructure.SseEmitterRegistry;
import com.gymmate.organisation.api.dto.GymSwitchResponse;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.security.dto.*;
import com.gymmate.shared.security.service.AuthenticationService;
import com.gymmate.shared.security.service.JwtService;
import com.gymmate.user.api.dto.GymAdminRegistrationRequest;
import com.gymmate.user.api.dto.MemberRegistrationRequest;
import com.gymmate.user.api.dto.UnifiedRegistrationRequest;
import com.gymmate.user.api.dto.UserRegistrationRequest;
import com.gymmate.user.api.dto.UserResponse;
import com.gymmate.user.domain.User;
import com.gymmate.user.domain.UserRole;
import com.gymmate.user.infrastructure.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

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
        private final GymService gymService;
        private final JwtService jwtService;
        private final UserRepository userRepository;
        private final SseEmitterRegistry sseEmitterRegistry;

        // ==================== REGISTRATION ====================

        @PostMapping("/register")
        public ResponseEntity<ApiResponse<UserResponse>> registerUser(
                        @Valid @RequestBody UserRegistrationRequest request) {
                User user = authenticationService.register(new UnifiedRegistrationRequest(
                                request.email(), request.firstName(), request.lastName(),
                                request.password(), request.phone(), request.role()));
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(UserResponse.fromEntity(user),
                                                "User registered successfully"));
        }

        @PostMapping("/register/member")
        public ResponseEntity<ApiResponse<UserResponse>> registerMember(
                        @Valid @RequestBody MemberRegistrationRequest request) {
                User user = authenticationService.register(new UnifiedRegistrationRequest(
                                request.email(), request.firstName(), request.lastName(),
                                request.password(), request.phone(), UserRole.MEMBER));
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(UserResponse.fromEntity(user),
                                                "Member registered successfully"));
        }

        @PostMapping("/register/gym-admin")
        public ResponseEntity<ApiResponse<UserResponse>> registerGymAdmin(
                        @Valid @RequestBody GymAdminRegistrationRequest request) {
                User user = authenticationService.register(new UnifiedRegistrationRequest(
                                request.email(), request.firstName(), request.lastName(),
                                request.password(), request.phone(), UserRole.OWNER));
                authenticationService.sendOtpForUser(user);
                return ResponseEntity.status(HttpStatus.CREATED)
                                .body(ApiResponse.success(UserResponse.fromEntity(user),
                                                "Gym owner registered successfully. An OTP has been sent to your email."));
        }

        // ==================== OTP VERIFICATION ====================

        @PostMapping("/register/resend-otp")
        public ResponseEntity<ApiResponse<RegistrationResponse>> resendOtp(
                        @Valid @RequestBody ResendOtpRequest request) {
                RegistrationResponse response = authenticationService.resendOtp(request);
                return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
        }

        @PostMapping("/register/verify-otp")
        public ResponseEntity<ApiResponse<VerificationTokenResponse>> verifyOtp(
                        @Valid @RequestBody VerifyOtpRequest request) {
                VerificationTokenResponse response = authenticationService.verifyOtp(request);
                return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
        }

        // ==================== EMAIL STATUS (SSE) ====================

        /**
         * SSE endpoint for real-time email delivery status.
         * The client subscribes after initiating registration or OTP resend
         * and receives events: SENDING → SENT or FAILED.
         */
        @GetMapping(value = "/email-status/{userId}", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
        @Operation(summary = "Email status stream", description = "Subscribe to real-time email delivery status updates")
        public SseEmitter streamEmailStatus(@PathVariable String userId) {
                return sseEmitterRegistry.createEmailStatusEmitter(userId);
        }

        // ==================== LOGIN / LOGOUT ====================

        @PostMapping("/login")
        public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
                LoginResponse response = authenticationService.authenticate(request);
                String message = response.isEmailVerified()
                                ? "Login successful"
                                : "Email not verified. An OTP has been sent to your email.";
                return ResponseEntity.ok(ApiResponse.success(response, message));
        }

        @PostMapping("/logout")
        public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String bearerToken) {
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

        // ==================== TOKEN MANAGEMENT ====================

        @PostMapping("/refresh")
        public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(
                        @Valid @RequestBody RefreshTokenRequest request) {
                TokenResponse response = authenticationService.refreshToken(request);
                return ResponseEntity.ok(ApiResponse.success(response, "Tokens refreshed successfully"));
        }

        // ==================== GYM CONTEXT ====================

        @PostMapping("/switch-gym/{gymId}")
        @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
        @Operation(summary = "Switch gym context", description = "Switch to a different gym within your organisation")
        public ResponseEntity<ApiResponse<GymSwitchResponse>> switchGym(
                        @PathVariable UUID gymId,
                        @RequestHeader("Authorization") String authHeader) {

                String token = authHeader.substring(7);
                UUID userId = jwtService.extractUserId(token);
                UUID organisationId = jwtService.extractOrganisationId(token);

                if (organisationId == null) {
                        throw new DomainException("NO_ORGANISATION", "User is not associated with an organisation");
                }

                Gym gym = gymService.getGymById(gymId);
                if (!organisationId.equals(gym.getOrganisationId())) {
                        throw new DomainException("GYM_ACCESS_DENIED",
                                        "The selected gym does not belong to your organisation");
                }

                User user = userRepository.findById(userId)
                                .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found"));

                String newAccessToken = jwtService.generateToken(user, gymId);
                String newRefreshToken = jwtService.generateRefreshToken(user);

                GymSwitchResponse response = GymSwitchResponse.builder()
                                .gymId(gym.getId())
                                .gymName(gym.getName())
                                .organisationId(organisationId)
                                .accessToken(newAccessToken)
                                .refreshToken(newRefreshToken)
                                .message("Switched to gym: " + gym.getName())
                                .build();

                return ResponseEntity.ok(ApiResponse.success(response, "Gym context switched successfully"));
        }

        @GetMapping("/current-gym")
        @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
        @Operation(summary = "Get current gym context")
        public ResponseEntity<ApiResponse<GymSwitchResponse>> getCurrentGym(
                        @RequestHeader("Authorization") String authHeader) {

                String token = authHeader.substring(7);
                UUID gymId = jwtService.extractGymId(token);
                UUID organisationId = jwtService.extractOrganisationId(token);

                if (gymId == null) {
                        return ResponseEntity.ok(ApiResponse.success(
                                        GymSwitchResponse.builder()
                                                        .organisationId(organisationId)
                                                        .message("No gym context set. Use /switch-gym/{gymId} to select a gym.")
                                                        .build()));
                }

                Gym gym = gymService.getGymById(gymId);
                GymSwitchResponse response = GymSwitchResponse.builder()
                                .gymId(gym.getId())
                                .gymName(gym.getName())
                                .organisationId(organisationId)
                                .message("Current gym: " + gym.getName())
                                .build();

                return ResponseEntity.ok(ApiResponse.success(response));
        }
}
