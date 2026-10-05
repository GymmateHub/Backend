package com.gymmate.billing.internal.domain;

import com.gymmate.shared.constants.SubscriptionStatus;
import com.gymmate.shared.domain.BaseAuditEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Subscription entity representing a subscription for an entire organisation.
 * Each organisation has one subscription that covers all their gyms/locations.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class Subscription extends BaseAuditEntity {

    private UUID organisationId;

    private SubscriptionTier tier;

    @Builder.Default
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    // Billing Period
    private LocalDateTime currentPeriodStart;

    private LocalDateTime currentPeriodEnd;

    @Builder.Default
    private Boolean cancelAtPeriodEnd = false;

    private LocalDateTime cancelledAt;

    // Trial Period
    private LocalDateTime trialStart;

    private LocalDateTime trialEnd;

    // Payment Integration
    private String stripeSubscriptionId;

    private String stripeCustomerId;

    private String paymentMethod;

    // Usage Tracking
    @Builder.Default
    private Integer currentMemberCount = 0;

    @Builder.Default
    private Integer currentLocationCount = 1;

    // Metadata
    private String metadata;

    // When this subscription first entered PAST_DUE — drives the grace-period
    // escalation in SubscriptionService.escalatePastDueSubscriptions. Null while not
    // past due.
    private LocalDateTime pastDueSince;

    /** Also used by SubscriptionService.escalatePastDueSubscriptions — the single source of truth for this window. */
    public static final long PAST_DUE_GRACE_PERIOD_DAYS = 7;

    // Business Methods
    public boolean isActive() {
        return status.isActive();
    }

    public boolean canAccess() {
        if (status == SubscriptionStatus.PAST_DUE && pastDueSince != null
                && pastDueSince.plusDays(PAST_DUE_GRACE_PERIOD_DAYS).isBefore(LocalDateTime.now())) {
            // Belt-and-braces: closes the window between grace-period expiry and the
            // next hourly escalation run (SubscriptionScheduledTasks) actually flipping
            // status to SUSPENDED. SubscriptionStatus.canAccess() alone can't express
            // this — it has no notion of duration, only the status enum value.
            return false;
        }
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
        this.pastDueSince = null;
    }

    public void markExpired() {
        this.status = SubscriptionStatus.EXPIRED;
    }

    /**
     * Idempotent on the timestamp: a second failure while already PAST_DUE does not
     * push pastDueSince forward, since that would reset the grace-period clock every
     * time Stripe retries.
     */
    public void markPastDue() {
        if (this.status != SubscriptionStatus.PAST_DUE) {
            this.pastDueSince = LocalDateTime.now();
        }
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
            this.pastDueSince = null;
        }
    }
}
