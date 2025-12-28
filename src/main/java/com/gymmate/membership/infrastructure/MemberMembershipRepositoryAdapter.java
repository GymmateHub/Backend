package com.gymmate.membership.infrastructure;

import com.gymmate.membership.domain.MemberMembership;
import com.gymmate.membership.domain.MembershipStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing domain repository interface using JPA repository.
 * Following hexagonal architecture pattern.
 */
@Component
@RequiredArgsConstructor
public class MemberMembershipRepositoryAdapter implements MemberMembershipRepository {

  private final MemberMembershipJpaRepository jpaRepository;

  @Override
  public MemberMembership save(MemberMembership membership) {
    return jpaRepository.save(membership);
  }

  @Override
  public Optional<MemberMembership> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<MemberMembership> findByMemberId(UUID memberId) {
    return jpaRepository.findByMemberId(memberId);
  }

  @Override
  public Optional<MemberMembership> findActiveMembershipByMemberId(UUID memberId) {
    return jpaRepository.findActiveMembershipByMemberId(memberId, LocalDateTime.now());
  }

  @Override
  public List<MemberMembership> findByGymId(UUID gymId) {
    return jpaRepository.findByGymId(gymId);
  }

  @Override
  public List<MemberMembership> findByGymIdAndStatus(UUID gymId, MembershipStatus status) {
    return jpaRepository.findByGymIdAndStatus(gymId, status);
  }

  @Override
  public List<MemberMembership> findByMemberIdAndGymIdAndStatusIn(UUID memberId, UUID gymId, List<MembershipStatus> statuses) {
    return jpaRepository.findByMemberIdAndGymIdAndStatusIn(memberId, gymId, statuses);
  }

  @Override
  public Optional<MemberMembership> findByStripeSubscriptionId(String stripeSubscriptionId) {
    return jpaRepository.findByStripeSubscriptionId(stripeSubscriptionId);
  }

  @Override
  public List<MemberMembership> findExpiringMemberships(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
    return jpaRepository.findExpiringMemberships(gymId, startDate, endDate);
  }

  @Override
  public List<MemberMembership> findByPlanId(UUID planId) {
    return jpaRepository.findByPlanId(planId);
  }

  @Override
  public long countActiveByGymId(UUID gymId) {
    return jpaRepository.countActiveByGymId(gymId);
  }

  @Override
  public long countByPlanId(UUID planId) {
    return jpaRepository.countByPlanId(planId);
  }

  @Override
  public void delete(MemberMembership membership) {
    jpaRepository.delete(membership);
  }
}

