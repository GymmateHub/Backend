package com.gymmate.subscription.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponse {
    private UUID id;
    private UUID gymId;
    private String tierName;
    private String tierDisplayName;
    private String status;
    private LocalDateTime currentPeriodStart;
    private LocalDateTime currentPeriodEnd;
    private Boolean cancelAtPeriodEnd;
    private LocalDateTime cancelledAt;
    private LocalDateTime trialStart;
    private LocalDateTime trialEnd;
    private Integer currentMemberCount;
    private Integer maxMembers;
    private BigDecimal price;
    private String billingCycle;

    // Rate limit info
    private Integer apiRequestsPerHour;
    private Integer apiBurstLimit;

    // Communication credits
    private Integer smsCreditsPerMonth;
    private Integer emailCreditsPerMonth;

    // Status flags
    private Boolean isActive;
    private Boolean isInTrial;
    private Boolean hasExceededMemberLimit;
    private Integer memberOverage;

    // Stripe integration status
    private Boolean hasStripeSubscription;
    private Boolean hasPaymentMethod;

    // Computed helper fields for frontend
    private Long daysRemainingInTrial;
    private Long daysUntilRenewal;
}

