package com.gymmate.access.internal.repository;

import com.gymmate.access.internal.domain.DoorBenefit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface DoorBenefitRepository extends JpaRepository<DoorBenefit, UUID> {

  boolean existsByAccessPointId(UUID accessPointId);

  boolean existsByAccessPointIdAndMembershipPlanId(UUID accessPointId, UUID membershipPlanId);
}
