package com.gymmate.billing.internal.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class SubscriptionUsage extends BaseAuditEntity {

    private Subscription subscription;

    // Billing Period
    private LocalDateTime billingPeriodStart;

    private LocalDateTime billingPeriodEnd;

    // Member Usage
    @Builder.Default
    private Integer memberCount = 0;

    @Builder.Default
    private Integer memberOverage = 0;

    // Communication Usage
    @Builder.Default
    private Integer smsSent = 0;

    @Builder.Default
    private Integer smsOverage = 0;

    @Builder.Default
    private Integer emailSent = 0;

    @Builder.Default
    private Integer emailOverage = 0;

    // API Usage
    @Builder.Default
    private Integer apiRequests = 0;

    @Builder.Default
    private Integer apiRateLimitHits = 0;

    // Classes
    @Builder.Default
    private Integer classesCreated = 0;

    // Storage (in GB)
    @Builder.Default
    private BigDecimal storageUsed = BigDecimal.ZERO;

    // Calculated Costs
    private BigDecimal baseCost;

    @Builder.Default
    private BigDecimal overageCost = BigDecimal.ZERO;

    private BigDecimal totalCost;

    // Status
    @Builder.Default
    private Boolean isBilled = false;

    private LocalDateTime billedAt;

    // Business Methods
    public void incrementSmsSent() {
        this.smsSent++;
        calculateSmsOverage();
        recalculateTotalCost();
    }

    public void incrementEmailSent() {
        this.emailSent++;
        calculateEmailOverage();
        recalculateTotalCost();
    }

    public void incrementApiRequest() {
        this.apiRequests++;
    }

    public void recordRateLimitHit() {
        this.apiRateLimitHits++;
    }

    public void updateMemberCount(Integer count) {
        this.memberCount = count;
        calculateMemberOverage();
        recalculateTotalCost();
    }

    private void calculateMemberOverage() {
        SubscriptionTier tier = subscription.getTier();
        if (memberCount > tier.getMaxMembers()) {
            this.memberOverage = memberCount - tier.getMaxMembers();
        } else {
            this.memberOverage = 0;
        }
    }

    private void calculateSmsOverage() {
        SubscriptionTier tier = subscription.getTier();
        if (smsSent > tier.getSmsCreditsPerMonth()) {
            this.smsOverage = smsSent - tier.getSmsCreditsPerMonth();
        } else {
            this.smsOverage = 0;
        }
    }

    private void calculateEmailOverage() {
        SubscriptionTier tier = subscription.getTier();
        if (emailSent > tier.getEmailCreditsPerMonth()) {
            this.emailOverage = emailSent - tier.getEmailCreditsPerMonth();
        } else {
            this.emailOverage = 0;
        }
    }

    private void recalculateTotalCost() {
        SubscriptionTier tier = subscription.getTier();
        this.overageCost = tier.calculateOverageCost(memberOverage, smsOverage, emailOverage);
        this.totalCost = baseCost.add(overageCost);
    }

    public void markAsBilled() {
        this.isBilled = true;
        this.billedAt = LocalDateTime.now();
    }

    public boolean needsUpgradeNotification() {
        // Notify if overage is more than 50% of base cost
        return overageCost.compareTo(baseCost.multiply(new BigDecimal("0.5"))) > 0;
    }
}
