package com.gymmate.retail.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.retail.internal.domain.Supplier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for Supplier domain entity.
 */
public interface SupplierRepository extends DomainRepository<Supplier, UUID> {

  List<Supplier> findByOrganisationId(UUID organisationId);

  List<Supplier> findActiveByOrganisationId(UUID organisationId);

  List<Supplier> findPreferredByOrganisationId(UUID organisationId);

  Optional<Supplier> findByCode(String code);

  Optional<Supplier> findByOrganisationIdAndName(UUID organisationId, String name);

  long countByOrganisationId(UUID organisationId);

  boolean existsByCode(String code);
}
