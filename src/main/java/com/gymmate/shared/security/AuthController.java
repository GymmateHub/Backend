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

  /**
   * Register a new user.
   */
  @PostMapping("/register")
  public ResponseEntity<ApiResponse<UserResponse>> registerUser(@Valid @RequestBody UserRegistrationRequest request) {
    User user = authenticationService.registerUser(
      request.getEmail(),
      request.getFirstName(),
      request.getLastName(),
      request.getPassword(),
      request.getPhone(),
      request.getRole()
    );

    UserResponse response = UserResponse.fromEntity(user);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success(response, "User registered successfully"));
  }

  /**
   * Register a new gym member (convenience endpoint).
   */
  @PostMapping("/register/member")
  public ResponseEntity<ApiResponse<UserResponse>> registerMember(@Valid @RequestBody UserRegistrationRequest request) {
    User user = authenticationService.registerMember(
      request.getEmail(),
      request.getFirstName(),
      request.getLastName(),
      request.getPassword(),
      request.getPhone()
    );

    UserResponse response = UserResponse.fromEntity(user);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success(response, "Member registered successfully"));
  }

  /**
   * Register a new gym admin/owner (convenience endpoint).
   */
  @PostMapping("/register/gym-admin")
  public ResponseEntity<ApiResponse<UserResponse>> registerGymAdmin(@Valid @RequestBody UserRegistrationRequest request) {
    User user = authenticationService.registerGymAdmin(
      request.getEmail(),
      request.getFirstName(),
      request.getLastName(),
      request.getPassword(),
      request.getPhone()
    );

    UserResponse response = UserResponse.fromEntity(user);
    return ResponseEntity.status(HttpStatus.CREATED)
      .body(ApiResponse.success(response, "Gym admin registered successfully"));
  }

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
