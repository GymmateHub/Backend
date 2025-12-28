package com.gymmate.shared.security;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.security.dto.*;
import com.gymmate.user.api.dto.UserRegistrationRequest;
import com.gymmate.user.api.dto.UserResponse;
import com.gymmate.user.domain.User;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {
  private final AuthenticationService authenticationService;
  private final RegistrationService registrationService;

  // ==================== NEW GYM OWNER REGISTRATION FLOW ====================

  /**
   * Step 1: Initiate registration - Send OTP to email
   */
  @PostMapping("/register/initiate")
  public ResponseEntity<ApiResponse<RegistrationResponse>> initiateRegistration(
      @Valid @RequestBody InitiateRegistrationRequest request) {
    RegistrationResponse response = registrationService.initiateRegistration(request);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(response, response.getMessage()));
  }

  /**
   * Step 2: Resend OTP (rate limited to 60 seconds)
   */
  @PostMapping("/register/resend-otp")
  public ResponseEntity<ApiResponse<RegistrationResponse>> resendOtp(
      @Valid @RequestBody ResendOtpRequest request) {
    RegistrationResponse response = registrationService.resendOtp(request);
    return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
  }

  /**
   * Step 3: Verify OTP - Get verification token
   */
  @PostMapping("/register/verify-otp")
  public ResponseEntity<ApiResponse<VerificationTokenResponse>> verifyOtp(
      @Valid @RequestBody VerifyOtpRequest request) {
    VerificationTokenResponse response = registrationService.verifyOtp(request);
    return ResponseEntity.ok(ApiResponse.success(response, response.getMessage()));
  }

  /**
   * Step 4: Complete registration - Set password and create user
   */
  @PostMapping("/register/complete")
  public ResponseEntity<ApiResponse<UserResponse>> completeRegistration(
      @Valid @RequestBody CompleteRegistrationRequest request) {
    User user = registrationService.completeRegistration(request);
    UserResponse response = UserResponse.fromEntity(user);
    return ResponseEntity.status(HttpStatus.CREATED)
        .body(ApiResponse.success(response, "Registration completed successfully. Please login."));
  }

  // ==================== LEGACY REGISTRATION ENDPOINTS ====================


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

//  /**
//   * Register a new gym member (convenience endpoint).
//   */
//  @PostMapping("/register/member")
//  public ResponseEntity<ApiResponse<UserResponse>> registerMember(@Valid @RequestBody UserRegistrationRequest request) {
//    User user = authenticationService.registerMember(
//      request.email(),
//      request.firstName(),
//      request.lastName(),
//      request.password(),
//      request.phone()
//    );
//
//    UserResponse response = UserResponse.fromEntity(user);
//    return ResponseEntity.status(HttpStatus.CREATED)
//      .body(ApiResponse.success(response, "Member registered successfully"));
//  }

//  /**
//   * Register a new gym admin/owner (convenience endpoint).
//   */
//  @PostMapping("/register/gym-admin")
//  public ResponseEntity<ApiResponse<UserResponse>> registerGymAdmin(@Valid @RequestBody UserRegistrationRequest request) {
//    User user = authenticationService.registerGymAdmin(
//      request.email(),
//      request.firstName(),
//      request.lastName(),
//      request.password(),
//      request.phone()
//    );
//
//    UserResponse response = UserResponse.fromEntity(user);
//    return ResponseEntity.status(HttpStatus.CREATED)
//      .body(ApiResponse.success(response, "Gym admin registered successfully"));
//  }

  /// Login and Log-out
  @PostMapping("/login")
  public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
      LoginResponse response = authenticationService.authenticate(request);
      return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
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

}
