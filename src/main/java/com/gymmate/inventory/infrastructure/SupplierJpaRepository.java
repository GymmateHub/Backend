package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.Supplier;
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
public interface SupplierJpaRepository extends JpaRepository<Supplier, UUID> {

  List<Supplier> findByOrganisationId(UUID organisationId);

  @Query("SELECT s FROM Supplier s WHERE s.organisationId = :organisationId AND s.active = true")
  List<Supplier> findActiveByOrganisationId(@Param("organisationId") UUID organisationId);

  @Query("SELECT s FROM Supplier s WHERE s.organisationId = :organisationId AND s.preferred = true")
  List<Supplier> findPreferredByOrganisationId(@Param("organisationId") UUID organisationId);

  Optional<Supplier> findByCode(String code);

  Optional<Supplier> findByOrganisationIdAndName(UUID organisationId, String name);

  long countByOrganisationId(UUID organisationId);

  boolean existsByCode(String code);
}
