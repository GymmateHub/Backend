package com.gymmate.inventory.internal.service;

import com.gymmate.inventory.api.InventoryFacade;
import com.gymmate.inventory.internal.domain.InventoryItem;
import com.gymmate.inventory.internal.repository.InventoryItemJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryFacadeImpl implements InventoryFacade {

    private final InventoryItemJpaRepository inventoryItemJpaRepository;
    private final InventoryService inventoryService;

    @Override
    public long countLowStockItems(UUID gymId) {
        return inventoryItemJpaRepository.countByGymIdAndCurrentStockLessThanMinimumStock(gymId);
    }

    @Override
    public void recordSale(UUID itemId, int quantity, BigDecimal unitPrice, UUID customerId,
                            String referenceNumber, String notes) {
        inventoryService.recordSale(itemId, quantity, unitPrice, customerId, referenceNumber, notes);
    }

    @Override
    public InventoryItemSummary getItemSummary(UUID itemId) {
        InventoryItem item = inventoryService.getInventoryItemById(itemId);
        return new InventoryItemSummary(item.getId(), item.getSku(), item.getBarcode(), item.getUnitCost());
    }
}
