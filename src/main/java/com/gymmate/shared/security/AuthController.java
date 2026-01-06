package com.gymmate.shared.security;

import com.gymmate.gym.application.GymService;
import com.gymmate.gym.domain.Gym;
import com.gymmate.organisation.api.dto.GymSwitchResponse;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.security.dto.*;
import com.gymmate.user.api.dto.GymAdminRegistrationRequest;
import com.gymmate.user.api.dto.MemberRegistrationRequest;
import com.gymmate.user.api.dto.UserRegistrationRequest;
import com.gymmate.user.api.dto.UserResponse;
import com.gymmate.user.domain.User;
import com.gymmate.user.infrastructure.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization APIs")
public class AuthController {
  private final AuthenticationService authenticationService;
  private final GymService gymService;
  private final JwtService jwtService;
  private final UserRepository userRepository;

  /**
   * Step 2: Resend OTP (rate limited to 60 seconds)
   */
  @PostMapping("/register/resend-otp")
  public ResponseEntity<ApiResponse<RegistrationResponse>> resendOtp(
      @Valid @RequestBody ResendOtpRequest request) {
    RegistrationResponse response = authenticationService.resendOtp(request);
    return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
  }

  /**
   * Step 3: Verify OTP - Activate user account
   */
  @PostMapping("/register/verify-otp")
  public ResponseEntity<ApiResponse<VerificationTokenResponse>> verifyOtp(
      @Valid @RequestBody VerifyOtpRequest request) {
    VerificationTokenResponse response = authenticationService.verifyOtp(request);
    return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
  }

  /**
   * Register a new user.
   */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
    User user = authenticationService.registerUser(
      request.email(),
      request.firstName(),
      request.lastName(),
      request.password(),
      request.phone(),
      request.role()
    );

    UserResponse response = UserResponse.fromEntity(user);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success(response, "User registered successfully"));
  }

  /**
   * Register a new gym member (convenience endpoint).
   */
  @PostMapping("/register/member")
  public ResponseEntity<ApiResponse<UserResponse>> registerMember(@Valid @RequestBody MemberRegistrationRequest request) {
    User user = authenticationService.registerMember(
      request.email(),
      request.firstName(),
      request.lastName(),
      request.password(),
      request.phone()
    );

    UserResponse response = UserResponse.fromEntity(user);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success(response, "Member registered successfully"));
  }

  /**
   * Register a new gym owner (convenience endpoint).
   * Creates user as INACTIVE with emailVerified=false, then sends OTP.
   */
  @PostMapping("/register/gym-admin")
  public ResponseEntity<ApiResponse<UserResponse>> registerGymAdmin(@Valid @RequestBody GymAdminRegistrationRequest request) {
    User user = authenticationService.registerGymAdmin(
      request.email(),
      request.firstName(),
      request.lastName(),
      request.password(),
      request.phone()
    );

    // Send OTP to the newly created gym owner
    authenticationService.sendOtpForUser(user);

    UserResponse response = UserResponse.fromEntity(user);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success(response, "Gym owner registered successfully. An OTP has been sent to your email."));
  }

  /// Login and Log-out
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
      LoginResponse response = authenticationService.authenticate(request);

      // Return appropriate message based on email verification status
      String message = response.isEmailVerified()
          ? "Login successful"
          : "Email not verified. An OTP has been sent to your email. Please verify to continue.";

      return ResponseEntity.ok(ApiResponse.success(response, message));
  }

  @PostMapping("/logout")
  public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String bearerToken) {
    String token = bearerToken.substring(7); // Remove "Bearer " prefix
    authenticationService.logout(token);
    return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
  }

  /// Password Reset
  @PostMapping("/password-reset/request")
  public ResponseEntity<ApiResponse<Void>> requestPasswordReset(@Valid @RequestBody PasswordResetRequest request) {
      authenticationService.initiatePasswordReset(request);
      return ResponseEntity.ok(ApiResponse.success(null, "Password reset email sent"));
  }

  @PostMapping("/password-reset/confirm")
  public ResponseEntity<ApiResponse<Void>> confirmPasswordReset(@Valid @RequestBody PasswordResetConfirmRequest request) {
      authenticationService.confirmPasswordReset(request);
      return ResponseEntity.ok(ApiResponse.success(null, "Password reset successful"));
  }

  /// Refresh Token
  @PostMapping("/refresh")
  public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
      TokenResponse response = authenticationService.refreshToken(request);
      return ResponseEntity.ok(ApiResponse.success(response, "Tokens refreshed successfully"));
  }

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

      // Extract current user info from token
      String token = authHeader.substring(7);
      UUID userId = jwtService.extractUserId(token);
      UUID organisationId = jwtService.extractOrganisationId(token);

      if (organisationId == null) {
          throw new DomainException("NO_ORGANISATION", "User is not associated with an organisation");
      }

      // Verify gym exists and belongs to user's organisation
      Gym gym = gymService.getGymById(gymId);
      if (!organisationId.equals(gym.getOrganisationId())) {
          throw new DomainException("GYM_ACCESS_DENIED",
              "The selected gym does not belong to your organisation");
      }

      // Get user for token generation
      User user = userRepository.findById(userId)
          .orElseThrow(() -> new DomainException("USER_NOT_FOUND", "User not found"));

      // Generate new tokens with gym context
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
