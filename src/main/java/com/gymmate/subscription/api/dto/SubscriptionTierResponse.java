package com.gymmate.subscription.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionTierResponse {
    private UUID id;
    private String name;
    private String displayName;
    private String description;
    private BigDecimal price;
    private String billingCycle;
    private Boolean isActive;
    private Boolean isFeatured;

    // Limits
    private Integer maxMembers;
    private Integer maxLocations;
    private Integer maxStaff;
    private Integer maxClassesPerMonth;

    // API Rate Limits
    private Integer apiRequestsPerHour;
    private Integer apiBurstLimit;
    private Integer concurrentConnections;

    // Communication Limits
    private Integer smsCreditsPerMonth;
    private Integer emailCreditsPerMonth;

    // Features
    private List<String> features;

    // Overage Pricing
    private BigDecimal overageMemberPrice;
    private BigDecimal overageSmsPrice;
    private BigDecimal overageEmailPrice;

    private Integer sortOrder;

    // Trial and Stripe configuration
    private Integer trialDays;
    private Boolean hasStripeIntegration;
}

