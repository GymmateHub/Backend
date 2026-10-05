package com.gymmate.retail.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for Supplier entity.
 */
@Repository
public interface SupplierJpaRepository extends JpaRepository<SupplierJpaEntity, UUID> {

    List<SupplierJpaEntity> findByOrganisationId(UUID organisationId);

    @Query("SELECT s FROM Supplier s WHERE s.organisationId = :organisationId AND s.active = true")
    List<SupplierJpaEntity> findActiveByOrganisationId(@Param("organisationId") UUID organisationId);

    @Query("SELECT s FROM Supplier s WHERE s.organisationId = :organisationId AND s.preferred = true")
    List<SupplierJpaEntity> findPreferredByOrganisationId(@Param("organisationId") UUID organisationId);

    Optional<SupplierJpaEntity> findByCode(String code);

    Optional<SupplierJpaEntity> findByOrganisationIdAndName(UUID organisationId, String name);

    long countByOrganisationId(UUID organisationId);

    boolean existsByCode(String code);
}
