package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.membership.internal.domain.MembershipStatus;
import com.gymmate.membership.internal.domain.MemberMembership;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link MemberMembership} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "MemberMembership")
@Table(name = "member_memberships")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(MemberMembership.class)
public class MemberMembershipJpaEntity extends GymScopedJpaEntity {

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "plan_id")
    private UUID membershipPlanId;

    // Subscription period
    @Column(name = "start_date", nullable = false)
    private LocalDate startDate;

    @Column(name = "end_date")
    private LocalDate endDate;

    // Billing
    @Column(name = "monthly_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal monthlyAmount;

    @Column(name = "billing_cycle", nullable = false, length = 20)
    private String billingCycle;

    @Column(name = "next_billing_date")
    private LocalDate nextBillingDate;

    // Usage tracking
    @Column(name = "class_credits_remaining")
    private Integer classCreditsRemaining;

    @Column(name = "guest_passes_remaining")
    private Integer guestPassesRemaining;

    @Column(name = "trainer_sessions_remaining")
    private Integer trainerSessionsRemaining;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private MembershipStatus status = MembershipStatus.ACTIVE;

    @Column(name = "auto_renew")
    private boolean autoRenew = true;

    // Stripe integration
    @Column(name = "stripe_customer_id")
    private String stripeCustomerId;

    @Column(name = "stripe_subscription_id")
    private String stripeSubscriptionId;

    // Freezing/holding
    @Column(name = "is_frozen")
    private boolean frozen = false;

    @Column(name = "frozen_from")
    private LocalDate frozenFrom;

    @Column(name = "frozen_until")
    private LocalDate frozenUntil;

    @Column(name = "freeze_reason", columnDefinition = "TEXT")
    private String freezeReason;

    @Column(name = "total_days_frozen")
    private Integer totalDaysFrozen = 0;

    @Column(name = "freeze_count")
    private Integer freezeCount = 0;

    // When this membership first entered PAST_DUE — drives the grace-period
    // escalation in MembershipService.escalatePastDueMemberships. Null while not
    // past due.
    @Column(name = "past_due_since")
    private LocalDateTime pastDueSince;

    /**
     * Also used by MembershipService.escalatePastDueMemberships — the single source of truth for this window.
     */
    public static final long PAST_DUE_GRACE_PERIOD_DAYS = 7;
}
