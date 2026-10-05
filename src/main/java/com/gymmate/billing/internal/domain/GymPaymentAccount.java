package com.gymmate.billing.internal.domain;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Billing's view of a gym as a Stripe Connect payee: identity/contact data (owned by the
 * organisation module, read-only here) plus the Connect payout-account state that billing
 * maintains.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GymPaymentAccount {

    private UUID id;
    private UUID organisationId;
    private String name;
    private String contactEmail;

    private String stripeConnectAccountId;
    private Boolean stripeChargesEnabled;
    private Boolean stripePayoutsEnabled;
    private Boolean stripeDetailsSubmitted;
    private LocalDateTime stripeOnboardingCompletedAt;
}
