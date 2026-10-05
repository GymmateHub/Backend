package com.gymmate.retail.internal.application.port;

import com.gymmate.retail.internal.domain.Supplier;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

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
  
  List<Supplier> saveAll(Iterable<Supplier> entities);
  
  boolean existsById(UUID id);
  
  List<Supplier> findAll();
  
  List<Supplier> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void deleteAll(Iterable<Supplier> entities);
  
  Supplier saveAndFlush(Supplier entity);
  
  void flush();
  
  Page<Supplier> findAll(Pageable pageable);
}
