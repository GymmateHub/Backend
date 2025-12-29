package com.gymmate.shared.security;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.security.dto.*;
import com.gymmate.user.api.dto.GymAdminRegistrationRequest;
import com.gymmate.user.api.dto.MemberRegistrationRequest;
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

}
