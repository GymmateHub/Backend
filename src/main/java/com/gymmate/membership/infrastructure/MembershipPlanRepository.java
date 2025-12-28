package com.gymmate.membership.infrastructure;

import com.gymmate.membership.domain.MembershipPlan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for MembershipPlan domain entity.
 * Following hexagonal architecture pattern.
 */
public interface MembershipPlanRepository {

  MembershipPlan save(MembershipPlan membershipPlan);

  Optional<MembershipPlan> findById(UUID id);

  List<MembershipPlan> findByGymId(UUID gymId);

  List<MembershipPlan> findActiveByGymId(UUID gymId);

  List<MembershipPlan> findFeaturedByGymId(UUID gymId);

  Optional<MembershipPlan> findByGymIdAndName(UUID gymId, String name);

  void delete(MembershipPlan membershipPlan);

  boolean existsByGymIdAndName(UUID gymId, String name);
}
