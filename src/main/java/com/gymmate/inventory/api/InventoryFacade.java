package com.gymmate.inventory.api;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Public read/command surface for the inventory module — replaces the direct
 * {@code inventory.internal.repository.InventoryItemJpaRepository}/
 * {@code inventory.internal.service.InventoryService} imports that {@code analytics}
 * and {@code pos} used before this module's {@code .api}/{@code .internal} split (see
 * the Phase 0 ADR, decision 3 — this retires that Tier-1 debt for `inventory`).
 */
public interface InventoryFacade {

    /**
     * Count of items at a gym whose current stock has fallen below their configured
     * minimum — used by analytics for the "low stock" dashboard tile.
     */
    long countLowStockItems(UUID gymId);

    /**
     * Records a sale against an inventory item (decrements stock, logs a SALE stock
     * movement). Used by {@code pos} when a sale line item references an inventory
     * item. The underlying stock-movement record is an internal detail, not returned.
     */
    void recordSale(UUID itemId, int quantity, BigDecimal unitPrice, UUID customerId,
                     String referenceNumber, String notes);

    /**
     * Minimal read projection of an inventory item — only the fields any current
     * caller (pos, for backfilling sale-item SKU/barcode/cost) actually needs. Never
     * exposes the underlying JPA entity.
     *
     * @throws com.gymmate.shared.exception.ResourceNotFoundException if no item with this ID exists
     */
    InventoryItemSummary getItemSummary(UUID itemId);

    record InventoryItemSummary(UUID id, String sku, String barcode, BigDecimal unitCost) {
    }
}
