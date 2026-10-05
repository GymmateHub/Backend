package com.gymmate.billing.internal.application.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Response containing Stripe Connect onboarding URL and account info.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConnectOnboardingResponse {

    private String accountId;
    private String onboardingUrl;
}

