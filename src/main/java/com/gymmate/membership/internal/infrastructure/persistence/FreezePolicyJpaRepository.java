package com.gymmate.membership.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FreezePolicyJpaRepository extends JpaRepository<FreezePolicyJpaEntity, UUID> {

    @Query("SELECT fp FROM FreezePolicy fp WHERE fp.gymId = :gymId AND fp.active = true")
    Optional<FreezePolicyJpaEntity> findActiveByGymId(@Param("gymId") UUID gymId);

    @Query("SELECT fp FROM FreezePolicy fp WHERE fp.isDefaultPolicy = true AND fp.active = true")
    Optional<FreezePolicyJpaEntity> findDefaultPolicy();

    @Query("SELECT fp FROM FreezePolicy fp WHERE fp.organisationId = :organisationId AND fp.isDefaultPolicy = true AND fp.active = true")
    Optional<FreezePolicyJpaEntity> findDefaultPolicyByOrganisation(@Param("organisationId") UUID organisationId);

    Optional<FreezePolicyJpaEntity> findByGymIdAndActiveTrue(UUID gymId);

    Optional<FreezePolicyJpaEntity> findByOrganisationIdAndIsDefaultPolicyTrueAndActiveTrue(UUID organisationId);
}
