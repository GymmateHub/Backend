package com.gymmate.subscription.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Subscription entity representing a subscription for an entire organisation.
 * Each organisation has one subscription that covers all their gyms/locations.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "subscriptions")
public class Subscription extends BaseAuditEntity {

    @Column(name = "organisation_id", nullable = false, unique = true)
    private UUID organisationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tier_id", nullable = false)
    private SubscriptionTier tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    // Billing Period
    @Column(name = "current_period_start", nullable = false)
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end", nullable = false)
    private LocalDateTime currentPeriodEnd;

    @Column(name = "cancel_at_period_end")
    @Builder.Default
    private Boolean cancelAtPeriodEnd = false;

    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    // Trial Period
    @Column(name = "trial_start")
    private LocalDateTime trialStart;

    @Column(name = "trial_end")
    private LocalDateTime trialEnd;

    // Payment Integration
    @Column(name = "stripe_subscription_id", unique = true)
    private String stripeSubscriptionId;

    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    // Usage Tracking
    @Column(name = "current_member_count")
    @Builder.Default
    private Integer currentMemberCount = 0;

    @Column(name = "current_location_count")
    @Builder.Default
    private Integer currentLocationCount = 1;

    // Metadata
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    // Business Methods
    public boolean isActive() {
        return status.isActive();
    }

    public boolean canAccess() {
        return status.canAccess() && !isExpired();
    }

    public boolean isExpired() {
        return currentPeriodEnd != null && currentPeriodEnd.isBefore(LocalDateTime.now());
    }

    public boolean isInTrial() {
        return status == SubscriptionStatus.TRIAL &&
               trialEnd != null &&
               trialEnd.isAfter(LocalDateTime.now());
    }

    public boolean hasExceededMemberLimit() {
        return currentMemberCount > tier.getMaxMembers();
    }

    public Integer getMemberOverage() {
        if (hasExceededMemberLimit()) {
            return currentMemberCount - tier.getMaxMembers();
        }
        return 0;
    }

    public void updateMemberCount(Integer count) {
        this.currentMemberCount = count;
    }

    public void cancelAtPeriodEnd() {
        this.cancelAtPeriodEnd = true;
        this.cancelledAt = LocalDateTime.now();
    }

    public void reactivate() {
        if (this.cancelAtPeriodEnd) {
            this.cancelAtPeriodEnd = false;
            this.cancelledAt = null;
        }
    }

    public void suspend() {
        this.status = SubscriptionStatus.SUSPENDED;
    }

    public void activate() {
        this.status = SubscriptionStatus.ACTIVE;
    }

    public void markExpired() {
        this.status = SubscriptionStatus.EXPIRED;
    }

    public void markPastDue() {
        this.status = SubscriptionStatus.PAST_DUE;
    }

    public void upgradeTier(SubscriptionTier newTier) {
        this.tier = newTier;
    }

    public void renewPeriod(LocalDateTime newStart, LocalDateTime newEnd) {
        this.currentPeriodStart = newStart;
        this.currentPeriodEnd = newEnd;
        if (this.status == SubscriptionStatus.EXPIRED) {
            this.status = SubscriptionStatus.ACTIVE;
        }
    }
}

