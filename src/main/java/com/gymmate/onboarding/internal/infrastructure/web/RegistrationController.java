package com.gymmate.onboarding.internal.infrastructure.web;

import com.gymmate.identity.api.IdentityApi;
import com.gymmate.onboarding.internal.application.dto.MemberRegistrationRequest;
import com.gymmate.onboarding.internal.application.dto.OwnerRegistrationRequest;
import com.gymmate.identity.api.dto.UserResponse;
import com.gymmate.onboarding.internal.application.OnboardingService;
import com.gymmate.shared.dto.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

/**
 * Self-service registration endpoints (public). Routes are unchanged from when they lived
 * on identity's AuthController.
 */
@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Authentication", description = "Authentication and authorization APIs")
public class RegistrationController {

    private final OnboardingService onboardingService;
    private final IdentityApi identityApi;

    @PostMapping("/register/owner")
    public ResponseEntity<ApiResponse<UserResponse>> registerOwner(
            @Valid @RequestBody OwnerRegistrationRequest request) {
        UUID userId = onboardingService.registerOwner(request);
        identityApi.sendRegistrationOtp(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(identityApi.describeUser(userId),
                        "Gym owner registered successfully. An OTP has been sent to your email."));
    }

    @PostMapping("/register/member")
    public ResponseEntity<ApiResponse<UserResponse>> registerMember(
            @Valid @RequestBody MemberRegistrationRequest request) {
        UUID userId = onboardingService.registerMember(request);
        identityApi.sendRegistrationOtp(userId);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(identityApi.describeUser(userId),
                        "Member registered successfully. An OTP has been sent to your email."));
    }
}
