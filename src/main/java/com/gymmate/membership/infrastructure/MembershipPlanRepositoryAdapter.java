package com.gymmate.membership.infrastructure;

import com.gymmate.membership.domain.MembershipPlan;
import com.gymmate.membership.domain.MembershipPlanRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing domain repository interface using JPA repository.
 * Following hexagonal architecture pattern.
 */
@Component
@RequiredArgsConstructor
public class MembershipPlanRepositoryAdapter implements MembershipPlanRepository {

  private final MembershipPlanJpaRepository jpaRepository;

  @Override
  public MembershipPlan save(MembershipPlan membershipPlan) {
    return jpaRepository.save(membershipPlan);
  }

  @Override
  public Optional<MembershipPlan> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<MembershipPlan> findByGymId(UUID gymId) {
    return jpaRepository.findByGymId(gymId);
  }

  @Override
  public List<MembershipPlan> findActiveByGymId(UUID gymId) {
    return jpaRepository.findActiveByGymId(gymId);
  }

  @Override
  public List<MembershipPlan> findFeaturedByGymId(UUID gymId) {
    return jpaRepository.findFeaturedByGymId(gymId);
  }

  @Override
  public Optional<MembershipPlan> findByGymIdAndName(UUID gymId, String name) {
    return jpaRepository.findByGymIdAndName(gymId, name);
  }

  @Override
  public void delete(MembershipPlan membershipPlan) {
    jpaRepository.delete(membershipPlan);
  }

  @Override
  public boolean existsByGymIdAndName(UUID gymId, String name) {
    return jpaRepository.existsByGymIdAndName(gymId, name);
  }
}

