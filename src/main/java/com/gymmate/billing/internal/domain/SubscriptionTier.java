package com.gymmate.billing.internal.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import lombok.*;

import java.math.BigDecimal;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class SubscriptionTier extends BaseAuditEntity {

    private String name;

    private String displayName;

    private String description;

    private BigDecimal price;

    @Builder.Default
    private 
    String billingCycle = "monthly"; // monthly, annual

    @Builder.Default
    private 
    Boolean active = true;

    @Builder.Default
    private 
    Boolean featured = false;

    // Limits
    private Integer maxMembers;

    @Builder.Default
    private 
    Integer maxLocations = 1;

    private Integer maxStaff;

    private Integer maxClassesPerMonth;

    // API Rate Limits
    @Builder.Default
    private 
    Integer apiRequestsPerHour = 1000;

    @Builder.Default
    private 
    Integer apiBurstLimit = 100;

    @Builder.Default
    private 
    Integer concurrentConnections = 10;

    // Communication Limits
    @Builder.Default
    private 
    Integer smsCreditsPerMonth = 0;

    @Builder.Default
    private 
    Integer emailCreditsPerMonth = 0;

    // Feature Flags

    @Builder.Default
    private String features = "[]";

    // Overage Pricing
    @Builder.Default
    private 
    BigDecimal overageMemberPrice = new BigDecimal("2.00");

    @Builder.Default
    private 
    BigDecimal overageSmsPrice = new BigDecimal("0.05");

    @Builder.Default
    private 
    BigDecimal overageEmailPrice = new BigDecimal("0.02");

    // Metadata
    @Builder.Default
    private 
    Integer sortOrder = 0;

    private 
    String metadata;

    // Stripe Integration
    private String stripeProductId;

    private String stripePriceId;

    @Builder.Default
    private 
    Integer trialDays = 14;

    // Business methods
    public boolean hasFeature(String feature) {
        // Parse JSON features array and check if feature exists
        return features != null && features.contains(feature);
    }

    public boolean allowsUnlimitedMembers() {
        return maxMembers >= 999999;
    }

    public boolean isStarterPlan() {
        return "starter".equalsIgnoreCase(name);
    }

    public boolean isProfessionalPlan() {
        return "professional".equalsIgnoreCase(name);
    }

    public boolean isEnterprisePlan() {
        return "enterprise".equalsIgnoreCase(name) || "custom".equalsIgnoreCase(name);
    }

    public BigDecimal calculateOverageCost(Integer memberOverage, Integer smsOverage, Integer emailOverage) {
        BigDecimal cost = BigDecimal.ZERO;

        if (memberOverage != null && memberOverage > 0) {
            cost = cost.add(overageMemberPrice.multiply(new BigDecimal(memberOverage)));
        }

        if (smsOverage != null && smsOverage > 0) {
            cost = cost.add(overageSmsPrice.multiply(new BigDecimal(smsOverage)));
        }

        if (emailOverage != null && emailOverage > 0) {
            cost = cost.add(overageEmailPrice.multiply(new BigDecimal(emailOverage)));
        }

        return cost;
    }
}
