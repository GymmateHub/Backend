package com.gymmate.membership.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * FreezePolicy entity defining business rules for membership freezing.
 * Can be configured per gym or use default policies.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "freeze_policies")
public class FreezePolicy extends GymScopedEntity {

  @Column(name = "policy_name", nullable = false)
  private String policyName;

  @Column(name = "max_freeze_days_per_year")
  @Builder.Default
  private Integer maxFreezeDaysPerYear = 90; // Default: 90 days per year

  @Column(name = "max_consecutive_freeze_days")
  @Builder.Default
  private Integer maxConsecutiveFreezeDays = 60; // Default: max 60 days per freeze

  @Column(name = "min_membership_days_before_freeze")
  @Builder.Default
  private Integer minMembershipDaysBeforeFreeze = 30; // Default: must be member for 30 days

  @Column(name = "cooling_off_period_days")
  @Builder.Default
  private Integer coolingOffPeriodDays = 30; // Default: 30 days between freezes

  @Column(name = "freeze_fee_amount")
  @Builder.Default
  private Double freezeFeeAmount = 0.0; // Default: no fee

  @Column(name = "freeze_fee_frequency")
  @Builder.Default
  private String freezeFeeFrequency = "NONE"; // NONE, ONE_TIME, MONTHLY

  @Column(name = "allow_partial_month_freeze")
  @Builder.Default
  private Boolean allowPartialMonthFreeze = true;

  @Column(name = "is_default_policy")
  @Builder.Default
  private Boolean isDefaultPolicy = false;

  public boolean canFreeze(MemberMembership membership) {
    // Check if membership has been active long enough
    if (minMembershipDaysBeforeFreeze != null && membership.getStartDate() != null) {
      long daysSinceStart = java.time.temporal.ChronoUnit.DAYS.between(
        membership.getStartDate(),
        java.time.LocalDate.now()
      );
      if (daysSinceStart < minMembershipDaysBeforeFreeze) {
        return false;
      }
    }

    // Check if member has exceeded yearly freeze limit
    if (maxFreezeDaysPerYear != null && membership.getTotalDaysFrozen() != null) {
      if (membership.getTotalDaysFrozen() >= maxFreezeDaysPerYear) {
        return false;
      }
    }

    return true;
  }

  public boolean isFreezeDurationValid(int requestedDays) {
    if (maxConsecutiveFreezeDays != null) {
      return requestedDays <= maxConsecutiveFreezeDays;
    }
    return true;
  }

  public int getRemainingFreezeDays(MemberMembership membership) {
    if (maxFreezeDaysPerYear == null) {
      return Integer.MAX_VALUE;
    }
    int used = membership.getTotalDaysFrozen() != null ? membership.getTotalDaysFrozen() : 0;
    return Math.max(0, maxFreezeDaysPerYear - used);
  }
}
