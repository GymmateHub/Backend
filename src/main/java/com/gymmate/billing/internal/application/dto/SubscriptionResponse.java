package com.gymmate.billing.internal.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record SubscriptionResponse(
        UUID id,
        UUID organisationId,
        String tierName,
        String tierDisplayName,
        String status,
        LocalDateTime currentPeriodStart,
        LocalDateTime currentPeriodEnd,
        Boolean cancelAtPeriodEnd,
        LocalDateTime cancelledAt,
        LocalDateTime trialStart,
        LocalDateTime trialEnd,
        Integer currentMemberCount,
        Integer maxMembers,
        BigDecimal price,
        String billingCycle,
        // Rate limit info
        Integer apiRequestsPerHour,
        Integer apiBurstLimit,
        // Communication credits
        Integer smsCreditsPerMonth,
        Integer emailCreditsPerMonth,
        // Status flags
        Boolean isActive,
        Boolean isInTrial,
        Boolean hasExceededMemberLimit,
        Integer memberOverage,
        // Stripe integration status
        Boolean hasStripeSubscription,
        Boolean hasPaymentMethod,
        // Computed helper fields for frontend
        Long daysRemainingInTrial,
        Long daysUntilRenewal
) {
}
