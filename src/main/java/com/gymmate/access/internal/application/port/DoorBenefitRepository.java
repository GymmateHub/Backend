package com.gymmate.access.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.access.internal.domain.DoorBenefit;

import java.util.UUID;

public interface DoorBenefitRepository extends DomainRepository<DoorBenefit, UUID> {

  boolean existsByAccessPointId(UUID accessPointId);

  boolean existsByAccessPointIdAndMembershipPlanId(UUID accessPointId, UUID membershipPlanId);
}
