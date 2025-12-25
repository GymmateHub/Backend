package com.gymmate.payment.api;

import com.gymmate.payment.api.dto.ConnectAccountStatusResponse;
import com.gymmate.payment.api.dto.ConnectOnboardingResponse;
import com.gymmate.payment.application.StripeConnectService;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

/**
 * REST controller for Stripe Connect onboarding and management.
 * Enables gyms to set up their payment accounts to receive member payments.
 */
@RestController
@RequestMapping("/api/connect")
@RequiredArgsConstructor
@Tag(name = "Stripe Connect", description = "Gym payment account setup and management")
public class ConnectController {

    private final StripeConnectService connectService;

    @PostMapping("/onboard")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Start onboarding", description = "Start Stripe Connect onboarding to accept member payments")
    public ResponseEntity<ApiResponse<ConnectOnboardingResponse>> startOnboarding() {
        UUID gymId = TenantContext.getCurrentTenantId();
        ConnectOnboardingResponse response = connectService.startOnboarding(gymId);
        return ResponseEntity.ok(ApiResponse.success(response, "Onboarding started. Redirect to the provided URL."));
    }

    @GetMapping("/status")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get account status", description = "Get the current Stripe Connect account status")
    public ResponseEntity<ApiResponse<ConnectAccountStatusResponse>> getAccountStatus() {
        UUID gymId = TenantContext.getCurrentTenantId();
        ConnectAccountStatusResponse response = connectService.getAccountStatus(gymId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    @PostMapping("/refresh")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Refresh onboarding link", description = "Get a new onboarding link if the previous one expired")
    public ResponseEntity<ApiResponse<ConnectOnboardingResponse>> refreshOnboardingLink() {
        UUID gymId = TenantContext.getCurrentTenantId();
        ConnectOnboardingResponse response = connectService.refreshOnboardingLink(gymId);
        return ResponseEntity.ok(ApiResponse.success(response, "New onboarding link generated."));
    }

    @GetMapping("/dashboard")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get dashboard link", description = "Get a link to the Stripe Express dashboard")
    public ResponseEntity<ApiResponse<String>> getDashboardLink() {
        UUID gymId = TenantContext.getCurrentTenantId();
        String dashboardUrl = connectService.getDashboardLink(gymId);
        return ResponseEntity.ok(ApiResponse.success(dashboardUrl, "Dashboard link generated."));
    }

    @GetMapping("/can-accept-payments")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('STAFF') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Check payment capability", description = "Check if the gym can accept member payments")
    public ResponseEntity<ApiResponse<Boolean>> canAcceptPayments() {
        UUID gymId = TenantContext.getCurrentTenantId();
        boolean canAccept = connectService.canAcceptPayments(gymId);
        return ResponseEntity.ok(ApiResponse.success(canAccept));
    }
}

