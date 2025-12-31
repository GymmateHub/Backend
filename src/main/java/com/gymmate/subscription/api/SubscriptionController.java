package com.gymmate.subscription.api;

import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.subscription.api.dto.*;
import com.gymmate.subscription.application.RateLimitService;
import com.gymmate.subscription.application.RateLimitStatistics;
import com.gymmate.subscription.application.RateLimitStatus;
import com.gymmate.subscription.application.SubscriptionService;
import com.gymmate.subscription.domain.Subscription;
import com.gymmate.subscription.domain.SubscriptionTier;
import com.gymmate.subscription.domain.SubscriptionUsage;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/subscriptions")
@RequiredArgsConstructor
@Tag(name = "Subscriptions", description = "SaaS Subscription Management APIs")
public class SubscriptionController {

    private final SubscriptionService subscriptionService;
    private final RateLimitService rateLimitService;
    private final SubscriptionMapper mapper;

    @PostMapping
    @PreAuthorize("hasRole('OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Create a new subscription", description = "Create a subscription for an organisation with optional Stripe billing")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> createSubscription(
            @Valid @RequestBody CreateSubscriptionRequest request) {

        UUID organisationId = TenantContext.getCurrentTenantId();
        Subscription subscription = subscriptionService.createSubscription(
            organisationId,
            request.getTierName(),
            Boolean.TRUE.equals(request.getStartTrial()),
            request.getPaymentMethodId(),
            request.getEnableStripeBilling() != null ? request.getEnableStripeBilling() : true
        );

        String message = Boolean.TRUE.equals(request.getStartTrial())
            ? "Subscription created with trial period"
            : "Subscription created successfully";

        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(mapper.toResponse(subscription), message));
    }

    @GetMapping("/current")
    @PreAuthorize("hasRole('OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get current subscription", description = "Get the current subscription for the organisation")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> getCurrentSubscription() {
        UUID organisationId = TenantContext.getCurrentTenantId();
        Subscription subscription = subscriptionService.getSubscription(organisationId);

        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(subscription)));
    }

    @PostMapping("/upgrade")
    @PreAuthorize("hasRole('OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Upgrade subscription", description = "Upgrade to a higher tier")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> upgradeSubscription(
            @Valid @RequestBody ChangeTierRequest request) {

        UUID organisationId = TenantContext.getCurrentTenantId();
        Subscription subscription = subscriptionService.upgradeSubscription(organisationId, request.getNewTierName());

        return ResponseEntity.ok(
            ApiResponse.success(mapper.toResponse(subscription), "Subscription upgraded successfully"));
    }

    @PostMapping("/downgrade")
    @PreAuthorize("hasRole('OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Downgrade subscription", description = "Downgrade to a lower tier")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> downgradeSubscription(
            @Valid @RequestBody ChangeTierRequest request) {

        UUID organisationId = TenantContext.getCurrentTenantId();
        Subscription subscription = subscriptionService.downgradeSubscription(organisationId, request.getNewTierName());

        return ResponseEntity.ok(
            ApiResponse.success(mapper.toResponse(subscription), "Subscription downgraded successfully"));
    }

    @PostMapping("/cancel")
    @PreAuthorize("hasRole('OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Cancel subscription", description = "Cancel the subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> cancelSubscription(
            @RequestParam(defaultValue = "false") boolean immediate) {

        UUID organisationId = TenantContext.getCurrentTenantId();
        Subscription subscription = subscriptionService.cancelSubscription(organisationId, immediate);

        String message = immediate
            ? "Subscription cancelled immediately"
            : "Subscription will be cancelled at the end of the billing period";

        return ResponseEntity.ok(ApiResponse.success(mapper.toResponse(subscription), message));
    }

    @PostMapping("/reactivate")
    @PreAuthorize("hasRole('OWNER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Reactivate subscription", description = "Reactivate a cancelled subscription")
    public ResponseEntity<ApiResponse<SubscriptionResponse>> reactivateSubscription() {
        UUID organisationId = TenantContext.getCurrentTenantId();
        Subscription subscription = subscriptionService.reactivateSubscription(organisationId);

        return ResponseEntity.ok(
            ApiResponse.success(mapper.toResponse(subscription), "Subscription reactivated successfully"));
    }

    @GetMapping("/tiers")
    @Operation(summary = "Get all subscription tiers", description = "Get all available subscription tiers")
    public ResponseEntity<ApiResponse<List<SubscriptionTierResponse>>> getAllTiers() {
        List<SubscriptionTier> tiers = subscriptionService.getAllActiveTiers();
        List<SubscriptionTierResponse> responses = tiers.stream()
            .map(mapper::toTierResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/tiers/featured")
    @Operation(summary = "Get featured tiers", description = "Get featured subscription tiers")
    public ResponseEntity<ApiResponse<List<SubscriptionTierResponse>>> getFeaturedTiers() {
        List<SubscriptionTier> tiers = subscriptionService.getFeaturedTiers();
        List<SubscriptionTierResponse> responses = tiers.stream()
            .map(mapper::toTierResponse)
            .collect(Collectors.toList());

        return ResponseEntity.ok(ApiResponse.success(responses));
    }

    @GetMapping("/usage/current")
    @PreAuthorize("hasRole('OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get current usage", description = "Get current billing period usage")
    public ResponseEntity<ApiResponse<SubscriptionUsage>> getCurrentUsage() {
        UUID organisationId = TenantContext.getCurrentTenantId();
        Subscription subscription = subscriptionService.getSubscription(organisationId);
        SubscriptionUsage usage = subscriptionService.getCurrentUsage(subscription.getId());

        return ResponseEntity.ok(ApiResponse.success(usage));
    }

    @GetMapping("/usage/history")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get usage history", description = "Get historical usage records")
    public ResponseEntity<ApiResponse<List<SubscriptionUsage>>> getUsageHistory() {
        UUID gymId = TenantContext.getCurrentTenantId();
        List<SubscriptionUsage> usageHistory = subscriptionService.getGymUsageHistory(gymId);

        return ResponseEntity.ok(ApiResponse.success(usageHistory));
    }

    @GetMapping("/rate-limit/status")
    @PreAuthorize("hasRole('GYM_OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get rate limit status", description = "Get current rate limit status")
    public ResponseEntity<ApiResponse<RateLimitStatus>> getRateLimitStatus() {
        UUID gymId = TenantContext.getCurrentTenantId();
        RateLimitStatus status = rateLimitService.getRateLimitStatus(gymId);

        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @GetMapping("/rate-limit/statistics")
    @PreAuthorize("hasRole('OWNER') or hasRole('MANAGER') or hasRole('SUPER_ADMIN')")
    @Operation(summary = "Get rate limit statistics", description = "Get rate limit statistics")
    public ResponseEntity<ApiResponse<RateLimitStatistics>> getRateLimitStatistics(
            @RequestParam(required = false) Integer days) {

        UUID organisationId = TenantContext.getCurrentTenantId();
        LocalDateTime since = days != null
            ? LocalDateTime.now().minusDays(days)
            : LocalDateTime.now().minusDays(30);

        RateLimitStatistics statistics = rateLimitService.getStatistics(organisationId, since);

        return ResponseEntity.ok(ApiResponse.success(statistics));
    }

    @PostMapping("/rate-limit/unblock")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @Operation(summary = "Unblock organisation", description = "Manually unblock a rate-limited organisation (Admin only)")
    public ResponseEntity<ApiResponse<Void>> unblockOrganisation(@RequestParam UUID organisationId) {
        rateLimitService.unblockOrganisation(organisationId);

        return ResponseEntity.ok(ApiResponse.success(null, "Organisation unblocked successfully"));
    }
}

