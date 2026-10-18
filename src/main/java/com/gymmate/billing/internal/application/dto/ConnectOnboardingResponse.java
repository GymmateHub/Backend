package com.gymmate.billing.internal.application.dto;

/**
 * Response containing Stripe Connect onboarding URL and account info.
 */
public record ConnectOnboardingResponse(
        String accountId,
        String onboardingUrl
) {
}
