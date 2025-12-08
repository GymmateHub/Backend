package com.gymmate.membership.infrastructure;

import com.gymmate.membership.domain.MembershipPlan;
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
public interface MembershipPlanJpaRepository extends JpaRepository<MembershipPlan, UUID> {

  List<MembershipPlan> findByGymId(UUID gymId);

  @Query("SELECT mp FROM MembershipPlan mp WHERE mp.gymId = :gymId AND mp.active = true")
  List<MembershipPlan> findActiveByGymId(@Param("gymId") UUID gymId);

  @Query("SELECT mp FROM MembershipPlan mp WHERE mp.gymId = :gymId AND mp.featured = true AND mp.active = true")
  List<MembershipPlan> findFeaturedByGymId(@Param("gymId") UUID gymId);

  Optional<MembershipPlan> findByGymIdAndName(UUID gymId, String name);

  boolean existsByGymIdAndName(UUID gymId, String name);
}
