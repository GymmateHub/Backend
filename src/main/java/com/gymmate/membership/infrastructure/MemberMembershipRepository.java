package com.gymmate.membership.infrastructure;

import com.gymmate.membership.domain.MemberMembership;
import com.gymmate.membership.domain.MembershipStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for MemberMembership domain entity.
 * Following hexagonal architecture pattern.
 */
public interface MemberMembershipRepository {

  MemberMembership save(MemberMembership membership);

  Optional<MemberMembership> findById(UUID id);

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

  void delete(MemberMembership membership);
}

