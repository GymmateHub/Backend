package com.gymmate.shared.security;

import com.gymmate.gym.application.GymService;
import com.gymmate.gym.domain.Gym;
import com.gymmate.organisation.api.dto.GymSwitchResponse;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.security.dto.*;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * Authentication Controller.
 *
 * Handles:
 * - Login/Logout
 * - Password reset
 * - Token refresh
 * - OTP verification (for registration flows)
 * - Gym context switching
 *
 * NOTE: Registration endpoints are handled by dedicated controllers:
 * - OwnerRegistrationController: /api/auth/register/owner
 * - MemberRegistrationController: /api/auth/register/member
 * - InviteController: /api/auth/invite/* (for ADMIN, TRAINER, STAFF)
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

  // ========== OTP Verification (used by registration flows) ==========

  /**
   * Resend OTP (rate limited to 60 seconds).
   * Used after owner/member registration to resend verification code.
   */
  @PostMapping("/register/resend-otp")
  @Operation(summary = "Resend OTP", description = "Resend OTP for email verification (rate limited)")
  public ResponseEntity<ApiResponse<RegistrationResponse>> resendOtp(
      @Valid @RequestBody ResendOtpRequest request) {
    RegistrationResponse response = authenticationService.resendOtp(request);
    return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
  }

  /**
   * Verify OTP - Activate user account.
   * Final step in owner/member registration flow.
   */
  @PostMapping("/register/verify-otp")
  @Operation(summary = "Verify OTP", description = "Verify OTP and activate user account")
  public ResponseEntity<ApiResponse<VerificationTokenResponse>> verifyOtp(
      @Valid @RequestBody VerifyOtpRequest request) {
    VerificationTokenResponse response = authenticationService.verifyOtp(request);
    return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
  }

  // ========== Login and Logout ==========

  @PostMapping("/login")
  @Operation(summary = "Login", description = "Authenticate user and get JWT tokens")
  public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
    LoginResponse response = authenticationService.authenticate(request);

    String message = response.isEmailVerified()
        ? "Login successful"
        : "Email not verified. An OTP has been sent to your email. Please verify to continue.";

    return ResponseEntity.ok(ApiResponse.success(response, message));
  }

  @PostMapping("/logout")
  @Operation(summary = "Logout", description = "Invalidate current JWT token")
  public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String bearerToken) {
    String token = bearerToken.substring(7); // Remove "Bearer " prefix
    authenticationService.logout(token);
    return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
  }

  // ========== Password Reset ==========

  @PostMapping("/password-reset/request")
  @Operation(summary = "Request password reset", description = "Send password reset email")
  public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
    authenticationService.initiatePasswordReset(request);
    return ResponseEntity.ok(ApiResponse.success(null, "Password reset email sent"));
  }

  @PostMapping("/password-reset/confirm")
  @Operation(summary = "Confirm password reset", description = "Reset password using token from email")
  public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
    authenticationService.confirmPasswordReset(request);
    return ResponseEntity.ok(ApiResponse.success(null, "Password reset successful"));
  }

  // ========== Token Refresh ==========

  @PostMapping("/refresh")
  @Operation(summary = "Refresh tokens", description = "Get new access token using refresh token")
  public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
    TokenResponse response = authenticationService.refreshToken(request);
    return ResponseEntity.ok(ApiResponse.success(response, "Tokens refreshed successfully"));
  }

  // ========== Gym Context Switching ==========

  /**
   * Switch gym context for the authenticated user.
   * Returns new JWT tokens with the selected gym context.
   * The gym must belong to the user's organisation.
   */
  @PostMapping("/switch-gym/{gymId}")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER')")
  @Operation(summary = "Switch gym context", description = "Switch to a different gym within your organisation and get new tokens")
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

  /**
   * Get current gym context from token.
   */
  @GetMapping("/current-gym")
  @PreAuthorize("hasAnyRole('OWNER', 'ADMIN', 'STAFF', 'TRAINER', 'MEMBER')")
  @Operation(summary = "Get current gym context", description = "Get the current gym context from the JWT token")
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
              .build()
      ));
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
