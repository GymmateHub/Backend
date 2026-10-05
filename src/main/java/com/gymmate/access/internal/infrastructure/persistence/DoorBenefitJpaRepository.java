package com.gymmate.access.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.UUID;

@Repository
public interface DoorBenefitJpaRepository extends JpaRepository<DoorBenefitJpaEntity, UUID> {

    boolean existsByAccessPointId(UUID accessPointId);

    boolean existsByAccessPointIdAndMembershipPlanId(UUID accessPointId, UUID membershipPlanId);
}
