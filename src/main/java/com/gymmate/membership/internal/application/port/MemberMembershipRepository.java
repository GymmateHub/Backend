package com.gymmate.membership.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.membership.internal.domain.MemberMembership;
import com.gymmate.membership.internal.domain.MembershipStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.math.BigDecimal;

/**
 * Repository interface for MemberMembership domain entity.
 * Following hexagonal architecture pattern.
 */
public interface MemberMembershipRepository extends DomainRepository<MemberMembership, UUID> {

  List<MemberMembership> findByMemberId(UUID memberId);

  Optional<MemberMembership> findActiveMembershipByMemberId(UUID memberId);

  List<MemberMembership> findByGymId(UUID gymId);

  List<MemberMembership> findByGymIdAndStatus(UUID gymId, MembershipStatus status);

  List<MemberMembership> findByMemberIdAndGymIdAndStatusIn(UUID memberId, UUID gymId, List<MembershipStatus> statuses);

  Optional<MemberMembership> findByStripeSubscriptionId(String stripeSubscriptionId);

  List<MemberMembership> findExpiringMemberships(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

  List<MemberMembership> findByPlanId(UUID planId);

  long countActiveByGymId(UUID gymId);

  long countByPlanId(UUID planId);

  List<MemberMembership> findFrozenMembershipsToUnfreeze(java.time.LocalDate date);

  List<MemberMembership> findStalePastDueMemberships(LocalDateTime cutoff);

  Optional<MemberMembership> findActiveMembershipByMemberId(UUID memberId, LocalDateTime now);

  List<MemberMembership> findExpiredActiveMemberships(LocalDateTime today);

  List<MemberMembership> findAutoRenewExpiredMemberships(LocalDateTime today);

  long countByGymIdAndStatus(UUID gymId, MembershipStatus status);

  long countCancelledByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

  List<Object[]> countActiveMembersByPlan(UUID gymId);

  BigDecimal sumProjectedRevenueByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);
}

