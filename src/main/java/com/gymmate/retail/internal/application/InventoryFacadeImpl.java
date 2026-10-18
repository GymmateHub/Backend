package com.gymmate.retail.internal.application;

import com.gymmate.retail.api.InventoryFacade;
import com.gymmate.retail.internal.domain.InventoryItem;
import com.gymmate.retail.internal.application.port.InventoryItemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class InventoryFacadeImpl implements InventoryFacade {

    private final InventoryItemRepository inventoryItemJpaRepository;
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
