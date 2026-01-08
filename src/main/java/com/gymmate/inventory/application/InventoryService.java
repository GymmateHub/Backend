package com.gymmate.inventory.application;

import com.gymmate.inventory.domain.InventoryItem;
import com.gymmate.inventory.domain.MovementType;
import com.gymmate.inventory.domain.StockMovement;
import com.gymmate.inventory.infrastructure.InventoryItemRepository;
import com.gymmate.inventory.infrastructure.StockMovementRepository;
import com.gymmate.shared.exception.DomainException;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service for inventory and stock management use cases.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
@Slf4j
public class InventoryService {

  private final InventoryItemRepository inventoryItemRepository;
  private final StockMovementRepository stockMovementRepository;

  // ===== Inventory Item Operations =====

  /**
   * Create new inventory item.
   */
  @Transactional
  public InventoryItem createInventoryItem(InventoryItem item) {
    // Validate SKU uniqueness if provided
    if (item.getSku() != null && inventoryItemRepository.existsBySku(item.getSku())) {
      throw new DomainException("DUPLICATE_SKU", "Inventory item with this SKU already exists");
    }

    log.info("Creating inventory item: {} for organisation: {}, gym: {}", 
      item.getName(), item.getOrganisationId(), item.getGymId());
    return inventoryItemRepository.save(item);
  }

  /**
   * Update inventory item.
   */
  @Transactional
  public InventoryItem updateInventoryItem(UUID id, InventoryItem updatedItem) {
    InventoryItem existing = getInventoryItemById(id);

    // Validate SKU uniqueness if changed
    if (updatedItem.getSku() != null && 
        !updatedItem.getSku().equals(existing.getSku()) &&
        inventoryItemRepository.existsBySku(updatedItem.getSku())) {
      throw new DomainException("DUPLICATE_SKU", "Inventory item with this SKU already exists");
    }

    // Update fields
    existing.setName(updatedItem.getName());
    existing.setSku(updatedItem.getSku());
    existing.setCategory(updatedItem.getCategory());
    existing.setDescription(updatedItem.getDescription());
    existing.setMinimumStock(updatedItem.getMinimumStock());
    existing.setMaximumStock(updatedItem.getMaximumStock());
    existing.setReorderPoint(updatedItem.getReorderPoint());
    existing.setReorderQuantity(updatedItem.getReorderQuantity());
    existing.setUnit(updatedItem.getUnit());
    existing.setBarcode(updatedItem.getBarcode());
    existing.setLocation(updatedItem.getLocation());
    existing.setExpiryTracking(updatedItem.isExpiryTracking());
    existing.setBatchTracking(updatedItem.isBatchTracking());
    existing.setImageUrl(updatedItem.getImageUrl());
    existing.setNotes(updatedItem.getNotes());

    log.info("Updated inventory item: {}", id);
    return inventoryItemRepository.save(existing);
  }

  /**
   * Update inventory item pricing.
   */
  @Transactional
  public InventoryItem updateItemPricing(UUID id, BigDecimal unitCost, BigDecimal unitPrice) {
    InventoryItem item = getInventoryItemById(id);
    item.updatePricing(unitCost, unitPrice);
    log.info("Updated pricing for inventory item: {}", id);
    return inventoryItemRepository.save(item);
  }

  /**
   * Delete inventory item.
   */
  @Transactional
  public void deleteInventoryItem(UUID id) {
    InventoryItem item = getInventoryItemById(id);
    inventoryItemRepository.delete(item);
    log.info("Deleted inventory item: {}", id);
  }

  /**
   * Get inventory item by ID.
   */
  public InventoryItem getInventoryItemById(UUID id) {
    return inventoryItemRepository.findById(id)
      .orElseThrow(() -> new ResourceNotFoundException("InventoryItem", id.toString()));
  }

  /**
   * Get inventory item by SKU.
   */
  public InventoryItem getInventoryItemBySku(String sku) {
    return inventoryItemRepository.findBySku(sku)
      .orElseThrow(() -> new ResourceNotFoundException("InventoryItem with SKU", sku));
  }

  /**
   * Get all inventory items for organisation.
   */
  public List<InventoryItem> getInventoryItemsByOrganisation(UUID organisationId) {
    return inventoryItemRepository.findByOrganisationId(organisationId);
  }

  /**
   * Get all inventory items for gym.
   */
  public List<InventoryItem> getInventoryItemsByGym(UUID gymId) {
    return inventoryItemRepository.findByGymId(gymId);
  }

  /**
   * Get active inventory items for organisation.
   */
  public List<InventoryItem> getActiveInventoryItemsByOrganisation(UUID organisationId) {
    return inventoryItemRepository.findActiveByOrganisationId(organisationId);
  }

  /**
   * Get active inventory items for gym.
   */
  public List<InventoryItem> getActiveInventoryItemsByGym(UUID gymId) {
    return inventoryItemRepository.findActiveByGymId(gymId);
  }

  /**
   * Get low stock items for gym.
   */
  public List<InventoryItem> getLowStockItemsByGym(UUID gymId) {
    List<InventoryItem> items = inventoryItemRepository.findLowStockByGymId(gymId);
    
    // Mark alerts as sent if not already
    items.forEach(item -> {
      if (!item.isLowStockAlertSent()) {
        log.warn("Low stock alert for item: {} ({}), current stock: {}, minimum: {}", 
          item.getName(), item.getSku(), item.getCurrentStock(), item.getMinimumStock());
      }
    });
    
    return items;
  }

  /**
   * Get items needing reorder for gym.
   */
  public List<InventoryItem> getReorderNeededByGym(UUID gymId) {
    return inventoryItemRepository.findReorderNeededByGymId(gymId);
  }

  // ===== Stock Movement Operations =====

  /**
   * Record stock purchase.
   */
  @Transactional
  public StockMovement recordPurchase(UUID itemId, int quantity, BigDecimal unitCost, 
                                       UUID supplierId, String referenceNumber, String notes) {
    InventoryItem item = getInventoryItemById(itemId);
    int stockBefore = item.getCurrentStock();
    
    item.increaseStock(quantity);
    inventoryItemRepository.save(item);

    StockMovement movement = StockMovement.builder()
      .inventoryItemId(itemId)
      .movementType(MovementType.PURCHASE)
      .quantity(quantity)
      .unitCost(unitCost)
      .totalCost(unitCost != null ? unitCost.multiply(BigDecimal.valueOf(quantity)) : null)
      .stockBefore(stockBefore)
      .stockAfter(item.getCurrentStock())
      .supplierId(supplierId)
      .referenceNumber(referenceNumber)
      .notes(notes)
      .build();

    movement.setOrganisationId(item.getOrganisationId());
    movement.setGymId(item.getGymId());

    log.info("Recorded purchase: {} units of item {}", quantity, itemId);
    return stockMovementRepository.save(movement);
  }

  /**
   * Record stock sale.
   */
  @Transactional
  public StockMovement recordSale(UUID itemId, int quantity, BigDecimal unitPrice, 
                                   UUID customerId, String referenceNumber, String notes) {
    InventoryItem item = getInventoryItemById(itemId);
    
    if (item.getCurrentStock() < quantity) {
      throw new DomainException("INSUFFICIENT_STOCK", 
        "Insufficient stock for item: " + item.getName() + 
        ". Available: " + item.getCurrentStock() + ", Requested: " + quantity);
    }

    int stockBefore = item.getCurrentStock();
    item.decreaseStock(quantity);
    inventoryItemRepository.save(item);

    StockMovement movement = StockMovement.builder()
      .inventoryItemId(itemId)
      .movementType(MovementType.SALE)
      .quantity(quantity)
      .unitCost(unitPrice)
      .totalCost(unitPrice != null ? unitPrice.multiply(BigDecimal.valueOf(quantity)) : null)
      .stockBefore(stockBefore)
      .stockAfter(item.getCurrentStock())
      .customerId(customerId)
      .referenceNumber(referenceNumber)
      .notes(notes)
      .build();

    movement.setOrganisationId(item.getOrganisationId());
    movement.setGymId(item.getGymId());

    log.info("Recorded sale: {} units of item {}", quantity, itemId);
    return stockMovementRepository.save(movement);
  }

  /**
   * Record stock adjustment.
   */
  @Transactional
  public StockMovement recordAdjustment(UUID itemId, int newStock, String reason, String performedBy) {
    InventoryItem item = getInventoryItemById(itemId);
    int stockBefore = item.getCurrentStock();
    
    item.setStock(newStock);
    inventoryItemRepository.save(item);

    int quantity = Math.abs(newStock - stockBefore);

    StockMovement movement = StockMovement.builder()
      .inventoryItemId(itemId)
      .movementType(MovementType.ADJUSTMENT)
      .quantity(quantity)
      .stockBefore(stockBefore)
      .stockAfter(newStock)
      .notes(reason)
      .performedBy(performedBy)
      .build();

    movement.setOrganisationId(item.getOrganisationId());
    movement.setGymId(item.getGymId());

    log.info("Recorded adjustment for item {}: {} -> {}", itemId, stockBefore, newStock);
    return stockMovementRepository.save(movement);
  }

  /**
   * Record damaged/written-off stock.
   */
  @Transactional
  public StockMovement recordDamage(UUID itemId, int quantity, String reason, String performedBy) {
    InventoryItem item = getInventoryItemById(itemId);
    
    if (item.getCurrentStock() < quantity) {
      throw new DomainException("INSUFFICIENT_STOCK", 
        "Cannot record damage: insufficient stock for item " + item.getName());
    }

    int stockBefore = item.getCurrentStock();
    item.decreaseStock(quantity);
    inventoryItemRepository.save(item);

    StockMovement movement = StockMovement.builder()
      .inventoryItemId(itemId)
      .movementType(MovementType.DAMAGE)
      .quantity(quantity)
      .stockBefore(stockBefore)
      .stockAfter(item.getCurrentStock())
      .notes(reason)
      .performedBy(performedBy)
      .build();

    movement.setOrganisationId(item.getOrganisationId());
    movement.setGymId(item.getGymId());

    log.info("Recorded damage: {} units of item {}", quantity, itemId);
    return stockMovementRepository.save(movement);
  }

  /**
   * Record stock transfer between gyms.
   */
  @Transactional
  public StockMovement recordTransfer(UUID itemId, int quantity, UUID fromGymId, UUID toGymId, 
                                       String notes, String performedBy) {
    InventoryItem item = getInventoryItemById(itemId);
    
    if (!item.getGymId().equals(fromGymId)) {
      throw new DomainException("INVALID_TRANSFER", 
        "Item does not belong to source gym");
    }

    if (item.getCurrentStock() < quantity) {
      throw new DomainException("INSUFFICIENT_STOCK", 
        "Insufficient stock for transfer");
    }

    int stockBefore = item.getCurrentStock();
    item.decreaseStock(quantity);
    inventoryItemRepository.save(item);

    StockMovement movement = StockMovement.builder()
      .inventoryItemId(itemId)
      .movementType(MovementType.TRANSFER_OUT)
      .quantity(quantity)
      .stockBefore(stockBefore)
      .stockAfter(item.getCurrentStock())
      .fromGymId(fromGymId)
      .toGymId(toGymId)
      .notes(notes)
      .performedBy(performedBy)
      .build();

    movement.setOrganisationId(item.getOrganisationId());
    movement.setGymId(fromGymId);

    log.info("Recorded transfer: {} units from gym {} to gym {}", quantity, fromGymId, toGymId);
    return stockMovementRepository.save(movement);
  }

  /**
   * Get stock movements for item.
   */
  public List<StockMovement> getStockMovementsByItem(UUID itemId) {
    return stockMovementRepository.findByInventoryItemIdOrderByMovementDateDesc(itemId);
  }

  /**
   * Get stock movements for gym.
   */
  public List<StockMovement> getStockMovementsByGym(UUID gymId) {
    return stockMovementRepository.findByGymId(gymId);
  }

  /**
   * Get stock movements for gym within date range.
   */
  public List<StockMovement> getStockMovementsByGymAndDateRange(
      UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
    return stockMovementRepository.findByGymIdAndDateRange(gymId, startDate, endDate);
  }

  /**
   * Get stock movements by type for gym.
   */
  public List<StockMovement> getStockMovementsByGymAndType(UUID gymId, MovementType type) {
    return stockMovementRepository.findByGymIdAndMovementType(gymId, type);
  }
}
