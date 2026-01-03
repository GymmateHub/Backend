package com.gymmate.subscription.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "subscription_usage")
public class SubscriptionUsage extends BaseAuditEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "subscription_id", nullable = false)
    private Subscription subscription;

    // Billing Period
    @Column(name = "billing_period_start", nullable = false)
    private LocalDateTime billingPeriodStart;

    @Column(name = "billing_period_end", nullable = false)
    private LocalDateTime billingPeriodEnd;

    // Member Usage
    @Column(name = "member_count")
    @Builder.Default
    private Integer memberCount = 0;

    @Column(name = "member_overage")
    @Builder.Default
    private Integer memberOverage = 0;

    // Communication Usage
    @Column(name = "sms_sent")
    @Builder.Default
    private Integer smsSent = 0;

    @Column(name = "sms_overage")
    @Builder.Default
    private Integer smsOverage = 0;

    @Column(name = "email_sent")
    @Builder.Default
    private Integer emailSent = 0;

    @Column(name = "email_overage")
    @Builder.Default
    private Integer emailOverage = 0;

    // API Usage
    @Column(name = "api_requests")
    @Builder.Default
    private Integer apiRequests = 0;

    @Column(name = "api_rate_limit_hits")
    @Builder.Default
    private Integer apiRateLimitHits = 0;

    // Classes
    @Column(name = "classes_created")
    @Builder.Default
    private Integer classesCreated = 0;

    // Storage (in GB)
    @Column(name = "storage_used", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal storageUsed = BigDecimal.ZERO;

    // Calculated Costs
    @Column(name = "base_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal baseCost;

    @Column(name = "overage_cost", precision = 10, scale = 2)
    @Builder.Default
    private BigDecimal overageCost = BigDecimal.ZERO;

    @Column(name = "total_cost", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalCost;

    // Status
    @Column(name = "is_billed")
    @Builder.Default
    private Boolean isBilled = false;

    @Column(name = "billed_at")
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

