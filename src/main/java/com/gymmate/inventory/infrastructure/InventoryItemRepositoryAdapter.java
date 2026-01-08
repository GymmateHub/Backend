package com.gymmate.inventory.infrastructure;

import com.gymmate.inventory.domain.InventoryItem;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter for InventoryItem repository.
 */
@Component
@RequiredArgsConstructor
public class InventoryItemRepositoryAdapter implements InventoryItemRepository {

  private final InventoryItemJpaRepository jpaRepository;

  @Override
  public InventoryItem save(InventoryItem inventoryItem) {
    return jpaRepository.save(inventoryItem);
  }

  @Override
  public Optional<InventoryItem> findById(UUID id) {
    return jpaRepository.findById(id);
  }

  @Override
  public List<InventoryItem> findByOrganisationId(UUID organisationId) {
    return jpaRepository.findByOrganisationId(organisationId);
  }

  @Override
  public List<InventoryItem> findByGymId(UUID gymId) {
    return jpaRepository.findByGymId(gymId);
  }

  @Override
  public Optional<InventoryItem> findBySku(String sku) {
    return jpaRepository.findBySku(sku);
  }

  @Override
  public List<InventoryItem> findActiveByGymId(UUID gymId) {
    return jpaRepository.findActiveByGymId(gymId);
  }

  @Override
  public List<InventoryItem> findActiveByOrganisationId(UUID organisationId) {
    return jpaRepository.findActiveByOrganisationId(organisationId);
  }

  @Override
  public List<InventoryItem> findLowStockByGymId(UUID gymId) {
    return jpaRepository.findLowStockByGymId(gymId);
  }

  @Override
  public List<InventoryItem> findLowStockByOrganisationId(UUID organisationId) {
    return jpaRepository.findLowStockByOrganisationId(organisationId);
  }

  @Override
  public List<InventoryItem> findReorderNeededByGymId(UUID gymId) {
    return jpaRepository.findReorderNeededByGymId(gymId);
  }

  @Override
  public List<InventoryItem> findReorderNeededByOrganisationId(UUID organisationId) {
    return jpaRepository.findReorderNeededByOrganisationId(organisationId);
  }

  @Override
  public void delete(InventoryItem inventoryItem) {
    jpaRepository.delete(inventoryItem);
  }

  @Override
  public long countByGymId(UUID gymId) {
    return jpaRepository.countByGymId(gymId);
  }

  @Override
  public long countByOrganisationId(UUID organisationId) {
    return jpaRepository.countByOrganisationId(organisationId);
  }

  @Override
  public boolean existsBySku(String sku) {
    return jpaRepository.existsBySku(sku);
  }
}
