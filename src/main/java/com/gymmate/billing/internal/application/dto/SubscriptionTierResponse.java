package com.gymmate.billing.internal.application.dto;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public record SubscriptionTierResponse(
        UUID id,
        String name,
        String displayName,
        String description,
        BigDecimal price,
        String billingCycle,
        Boolean isActive,
        Boolean isFeatured,
        // Limits
        Integer maxMembers,
        Integer maxLocations,
        Integer maxStaff,
        Integer maxClassesPerMonth,
        // API Rate Limits
        Integer apiRequestsPerHour,
        Integer apiBurstLimit,
        Integer concurrentConnections,
        // Communication Limits
        Integer smsCreditsPerMonth,
        Integer emailCreditsPerMonth,
        // Features
        List<String> features,
        // Overage Pricing
        BigDecimal overageMemberPrice,
        BigDecimal overageSmsPrice,
        BigDecimal overageEmailPrice,
        Integer sortOrder,
        // Trial and Stripe configuration
        Integer trialDays,
        Boolean hasStripeIntegration
) {
}
