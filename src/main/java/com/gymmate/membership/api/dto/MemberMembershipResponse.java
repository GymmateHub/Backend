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
  public static MemberMembershipResponse from(MemberMembership membership) {
    return new MemberMembershipResponse(
      membership.getId(),
      membership.getGymId(),
      membership.getMemberId(),
      membership.getPlanId(),
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
}
