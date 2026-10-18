package com.gymmate.membership.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.membership.internal.domain.MembershipPlan;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for MembershipPlan domain entity.
 * Following hexagonal architecture pattern.
 */
public interface MembershipPlanRepository extends DomainRepository<MembershipPlan, UUID> {

  List<MembershipPlan> findByGymId(UUID gymId);

  List<MembershipPlan> findActiveByGymId(UUID gymId);

  List<MembershipPlan> findFeaturedByGymId(UUID gymId);

  Optional<MembershipPlan> findByGymIdAndName(UUID gymId, String name);

  boolean existsByGymIdAndName(UUID gymId, String name);
}
