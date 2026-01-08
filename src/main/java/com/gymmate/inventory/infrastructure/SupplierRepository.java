package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.Supplier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Supplier domain entity.
 */
public interface SupplierRepository {

  Supplier save(Supplier supplier);

  Optional<Supplier> findById(UUID id);

  List<Supplier> findByOrganisationId(UUID organisationId);

  List<Supplier> findActiveByOrganisationId(UUID organisationId);

  List<Supplier> findPreferredByOrganisationId(UUID organisationId);

  Optional<Supplier> findByCode(String code);

  Optional<Supplier> findByOrganisationIdAndName(UUID organisationId, String name);

  void delete(Supplier supplier);

  long countByOrganisationId(UUID organisationId);

  boolean existsByCode(String code);
}
