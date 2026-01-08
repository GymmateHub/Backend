package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.Supplier;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter for Supplier repository.
 */
@Component
@RequiredArgsConstructor
public class SupplierRepositoryAdapter implements SupplierRepository {

  private final SupplierJpaRepository jpaRepository;

  @Override
  public Supplier save(Supplier supplier) {
    return jpaRepository.save(supplier);
  }

  @Override
  public Optional<Supplier> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<Supplier> findByOrganisationId(UUID organisationId) {
    return jpaRepository.findByOrganisationId(organisationId);
  }

  @Override
  public List<Supplier> findActiveByOrganisationId(UUID organisationId) {
    return jpaRepository.findActiveByOrganisationId(organisationId);
  }

  @Override
  public List<Supplier> findPreferredByOrganisationId(UUID organisationId) {
    return jpaRepository.findPreferredByOrganisationId(organisationId);
  }

  @Override
  public Optional<Supplier> findByCode(String code) {
    return jpaRepository.findByCode(code);
  }

  @Override
  public Optional<Supplier> findByOrganisationIdAndName(UUID organisationId, String name) {
    return jpaRepository.findByOrganisationIdAndName(organisationId, name);
  }

  @Override
  public void delete(Supplier supplier) {
    jpaRepository.delete(supplier);
  }

  @Override
  public long countByOrganisationId(UUID organisationId) {
    return jpaRepository.countByOrganisationId(organisationId);
  }

  @Override
  public boolean existsByCode(String code) {
    return jpaRepository.existsByCode(code);
  }
}
