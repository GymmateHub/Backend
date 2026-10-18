package com.gymmate.membership.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for MembershipPlan entity.
 */
@Repository
public interface MembershipPlanJpaRepository extends JpaRepository<MembershipPlanJpaEntity, UUID> {

    List<MembershipPlanJpaEntity> findByGymId(UUID gymId);

    @Query("SELECT mp FROM MembershipPlan mp WHERE mp.gymId = :gymId AND mp.active = true")
    List<MembershipPlanJpaEntity> findActiveByGymId(@Param("gymId") UUID gymId);

    @Query("SELECT mp FROM MembershipPlan mp WHERE mp.gymId = :gymId AND mp.featured = true AND mp.active = true")
    List<MembershipPlanJpaEntity> findFeaturedByGymId(@Param("gymId") UUID gymId);

    Optional<MembershipPlanJpaEntity> findByGymIdAndName(UUID gymId, String name);

    boolean existsByGymIdAndName(UUID gymId, String name);
}
