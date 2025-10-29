package com.gymmate.shared.security;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.security.dto.*;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthenticationService authenticationService;

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<LoginResponse>> login(@Valid @RequestBody LoginRequest request) {
        LoginResponse response = authenticationService.authenticate(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Login successful"));
    }

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

    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<TokenResponse>> refreshToken(@Valid @RequestBody RefreshTokenRequest request) {
        TokenResponse response = authenticationService.refreshToken(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Tokens refreshed successfully"));
    }

    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@RequestHeader("Authorization") String bearerToken) {
        String token = bearerToken.substring(7); // Remove "Bearer " prefix
        authenticationService.logout(token);
        return ResponseEntity.ok(ApiResponse.success(null, "Logged out successfully"));
    }
}
