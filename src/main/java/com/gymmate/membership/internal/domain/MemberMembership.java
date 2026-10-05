package com.gymmate.membership.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * MemberMembership entity representing a member's subscription to a plan.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class MemberMembership extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
  private UUID memberId;

    private UUID membershipPlanId;

  // Subscription period
  private LocalDate startDate;

  private LocalDate endDate;

  // Billing
  private BigDecimal monthlyAmount;

  private String billingCycle;

  private LocalDate nextBillingDate;

  // Usage tracking
  private Integer classCreditsRemaining;

  private Integer guestPassesRemaining;

  private Integer trainerSessionsRemaining;

  // Status

  @Builder.Default
  private MembershipStatus status = MembershipStatus.ACTIVE;

  @Builder.Default
  private boolean autoRenew = true;

  // Stripe integration
  private String stripeCustomerId;

  private String stripeSubscriptionId;

  // Freezing/holding
  @Builder.Default
  private boolean frozen = false;

  private LocalDate frozenFrom;

  private LocalDate frozenUntil;

  private String freezeReason;

  @Builder.Default
  private Integer totalDaysFrozen = 0;

  @Builder.Default
  private Integer freezeCount = 0;

  // When this membership first entered PAST_DUE — drives the grace-period
  // escalation in MembershipService.escalatePastDueMemberships. Null while not
  // past due.
  private LocalDateTime pastDueSince;

  /** Also used by MembershipService.escalatePastDueMemberships — the single source of truth for this window. */
  public static final long PAST_DUE_GRACE_PERIOD_DAYS = 7;

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
    LocalDate now = LocalDate.now();
    this.frozen = true;
    this.frozenFrom = now;
    this.frozenUntil = until;
    this.freezeReason = reason;
    this.status = MembershipStatus.PAUSED;
    this.freezeCount = (this.freezeCount == null ? 0 : this.freezeCount) + 1;
  }

  public void unfreeze() {
    if (this.frozenFrom != null && this.frozenUntil != null) {
      // Calculate days frozen and extend membership dates
      long daysFrozen = java.time.temporal.ChronoUnit.DAYS.between(this.frozenFrom, LocalDate.now());
      if (daysFrozen < 0) daysFrozen = 0;

      // Extend end date and next billing date by days frozen
      if (this.endDate != null) {
        this.endDate = this.endDate.plusDays(daysFrozen);
      }
      if (this.nextBillingDate != null) {
        this.nextBillingDate = this.nextBillingDate.plusDays(daysFrozen);
      }

      // Update total days frozen
      this.totalDaysFrozen = (this.totalDaysFrozen == null ? 0 : this.totalDaysFrozen) + (int) daysFrozen;
    }

    this.frozen = false;
    this.frozenFrom = null;
    this.frozenUntil = null;
    this.freezeReason = null;
    this.status = MembershipStatus.ACTIVE;
  }

  public int getFreezeDaysRemaining() {
    if (frozen && frozenFrom != null) {
      return (int) java.time.temporal.ChronoUnit.DAYS.between(LocalDate.now(), frozenUntil);
    }
    return 0;
  }

  public void cancel() {
    this.status = MembershipStatus.CANCELLED;
    this.autoRenew = false;
  }

  public void expire() {
    this.status = MembershipStatus.EXPIRED;
  }

  /**
   * Mark past due on a payment failure. Idempotent on the timestamp: a second failure
   * while already PAST_DUE does not push pastDueSince forward, since that would reset
   * the grace-period clock every time Stripe retries.
   */
  public void markPastDue() {
    if (this.status != MembershipStatus.PAST_DUE) {
      this.pastDueSince = LocalDateTime.now();
    }
    this.status = MembershipStatus.PAST_DUE;
  }

  /** Restore to ACTIVE after a payment succeeds, clearing the past-due clock. */
  public void clearPastDue() {
    this.pastDueSince = null;
    if (this.status == MembershipStatus.PAST_DUE) {
      this.status = MembershipStatus.ACTIVE;
    }
  }

  /** Suspend after PAST_DUE has exceeded the grace period. */
  public void suspend() {
    this.status = MembershipStatus.SUSPENDED;
  }

  public boolean isActive() {
    return status == MembershipStatus.ACTIVE && !frozen;
  }

  public boolean hasClassCreditsRemaining() {
    return classCreditsRemaining == null || classCreditsRemaining > 0;
  }
}
