package com.gymmate.membership.infrastructure;

import com.gymmate.membership.domain.FreezePolicy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface FreezePolicyJpaRepository extends JpaRepository<FreezePolicy, UUID> {

  @Query("SELECT fp FROM FreezePolicy fp WHERE fp.gymId = :gymId AND fp.active = true")
  Optional<FreezePolicy> findActiveByGymId(@Param("gymId") UUID gymId);

  @Query("SELECT fp FROM FreezePolicy fp WHERE fp.isDefaultPolicy = true AND fp.active = true")
  Optional<FreezePolicy> findDefaultPolicy();

  @Query("SELECT fp FROM FreezePolicy fp WHERE fp.organisationId = :organisationId AND fp.isDefaultPolicy = true AND fp.active = true")
  Optional<FreezePolicy> findDefaultPolicyByOrganisation(@Param("organisationId") UUID organisationId);
}
