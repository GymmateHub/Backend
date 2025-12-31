package com.gymmate.membership.domain;

import com.gymmate.shared.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "member_memberships")
public class MemberMembership extends TenantEntity {


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
  @Builder.Default
  private MembershipStatus status = MembershipStatus.ACTIVE;

  @Column(name = "auto_renew")
  @Builder.Default
  private boolean autoRenew = true;

  // Stripe integration
  @Column(name = "stripe_customer_id")
  private String stripeCustomerId;

  @Column(name = "stripe_subscription_id")
  private String stripeSubscriptionId;

  // Freezing/holding
  @Column(name = "is_frozen")
  @Builder.Default
  private boolean frozen = false;

  @Column(name = "frozen_until")
  private LocalDate frozenUntil;

  @Column(name = "freeze_reason", columnDefinition = "TEXT")
  private String freezeReason;

  public void useClassCredit() {
    if (classCreditsRemaining != null && classCreditsRemaining > 0) {
      classCreditsRemaining--;
    }
  }

  public void useGuestPass() {
    if (guestPassesRemaining != null && guestPassesRemaining > 0) {
      guestPassesRemaining--;
    }
  }

  public void useTrainerSession() {
    if (trainerSessionsRemaining != null && trainerSessionsRemaining > 0) {
      trainerSessionsRemaining--;
    }
  }

  public void freeze(LocalDate until, String reason) {
    this.frozen = true;
    this.frozenUntil = until;
    this.freezeReason = reason;
    this.status = MembershipStatus.PAUSED;
  }

  public void unfreeze() {
    this.frozen = false;
    this.frozenUntil = null;
    this.freezeReason = null;
    this.status = MembershipStatus.ACTIVE;
  }

  public void cancel() {
    this.status = MembershipStatus.CANCELLED;
    this.autoRenew = false;
  }

  public void expire() {
    this.status = MembershipStatus.EXPIRED;
  }

  public boolean isActive() {
    return status == MembershipStatus.ACTIVE && !frozen;
  }

  public boolean hasClassCreditsRemaining() {
    return classCreditsRemaining == null || classCreditsRemaining > 0;
  }
}

