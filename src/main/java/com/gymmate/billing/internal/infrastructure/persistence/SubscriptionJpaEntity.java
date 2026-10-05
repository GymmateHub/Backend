package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.constants.SubscriptionStatus;
import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.billing.internal.domain.Subscription;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link Subscription} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "Subscription")
@Table(name = "subscriptions")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(Subscription.class)
public class SubscriptionJpaEntity extends BaseAuditJpaEntity {

    @Column(name = "organisation_id", nullable = false, unique = true)
    private UUID organisationId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "tier_id", nullable = false)
    private SubscriptionTierJpaEntity tier;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private SubscriptionStatus status = SubscriptionStatus.ACTIVE;

    // Billing Period
    @Column(name = "current_period_start", nullable = false)
    private LocalDateTime currentPeriodStart;

    @Column(name = "current_period_end", nullable = false)
    private LocalDateTime currentPeriodEnd;

    @Column(name = "cancel_at_period_end")
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
    private Integer currentMemberCount = 0;

    @Column(name = "current_location_count")
    private Integer currentLocationCount = 1;

    // Metadata
    @JdbcTypeCode(SqlTypes.JSON)
    @Column(columnDefinition = "jsonb")
    private String metadata;

    // When this subscription first entered PAST_DUE — drives the grace-period
    // escalation in SubscriptionService.escalatePastDueSubscriptions. Null while not
    // past due.
    @Column(name = "past_due_since")
    private LocalDateTime pastDueSince;

    /**
     * Also used by SubscriptionService.escalatePastDueSubscriptions — the single source of truth for this window.
     */
    public static final long PAST_DUE_GRACE_PERIOD_DAYS = 7;
}
