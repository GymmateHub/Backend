package com.gymmate.membership.api.dto;

import com.gymmate.membership.domain.MemberMembership;
import com.gymmate.membership.domain.MembershipStatus;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record MemberMembershipResponse(
  UUID id,
  UUID gymId,
  UUID memberId,
  UUID planId,
  LocalDate startDate,
  LocalDate endDate,
  BigDecimal monthlyAmount,
  String billingCycle,
  LocalDate nextBillingDate,
  Integer classCreditsRemaining,
  Integer guestPassesRemaining,
  Integer trainerSessionsRemaining,
  MembershipStatus status,
  Boolean autoRenew,
  Boolean frozen,
  LocalDate frozenUntil,
  String freezeReason,
  LocalDateTime createdAt,
  LocalDateTime updatedAt
) {
  /**
   * Create response from membership entity.
   * Note: gymId is obtained through Member relationship (member.gymId)
   */
  public static MemberMembershipResponse from(MemberMembership membership, UUID gymId) {
    return new MemberMembershipResponse(
      membership.getId(),
      gymId,
      membership.getMemberId(),
      membership.getMembershipPlanId(),
      membership.getStartDate(),
      membership.getEndDate(),
      membership.getMonthlyAmount(),
      membership.getBillingCycle(),
      membership.getNextBillingDate(),
      membership.getClassCreditsRemaining(),
      membership.getGuestPassesRemaining(),
      membership.getTrainerSessionsRemaining(),
      membership.getStatus(),
      membership.isAutoRenew(),
      membership.isFrozen(),
      membership.getFrozenUntil(),
      membership.getFreezeReason(),
      membership.getCreatedAt(),
      membership.getUpdatedAt()
    );
  }

  /**
   * Create response from membership entity without gymId (sets null).
   * Used when gymId is not readily available in the controller context.
   */
  public static MemberMembershipResponse from(MemberMembership membership) {
    return from(membership, null);
  }
}
