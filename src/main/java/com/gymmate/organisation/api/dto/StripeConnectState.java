package com.gymmate.organisation.api.dto;

import java.time.LocalDateTime;

/** Stripe Connect payout-account state of a gym, as maintained by the billing module. */
public record StripeConnectState(
        String accountId,
        Boolean chargesEnabled,
        Boolean payoutsEnabled,
        Boolean detailsSubmitted,
        LocalDateTime onboardingCompletedAt) {
}
