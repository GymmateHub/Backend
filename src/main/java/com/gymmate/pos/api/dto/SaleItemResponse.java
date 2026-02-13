package com.gymmate.pos.api.dto;

import com.gymmate.pos.domain.SaleItem;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Response DTO for SaleItem entity.
 */
public record SaleItemResponse(
        UUID id,
        UUID inventoryItemId,
        String itemName,
        String itemSku,
        String itemBarcode,
        Integer quantity,
        BigDecimal unitPrice,
        BigDecimal costPrice,
        BigDecimal discountAmount,
        BigDecimal discountPercentage,
        BigDecimal lineTotal,
        String notes,
        boolean refunded,
        Integer refundedQuantity) {
    public static SaleItemResponse fromEntity(SaleItem item) {
        return new SaleItemResponse(
                item.getId(),
                item.getInventoryItemId(),
                item.getItemName(),
                item.getItemSku(),
                item.getItemBarcode(),
                item.getQuantity(),
                item.getUnitPrice(),
                item.getCostPrice(),
                item.getDiscountAmount(),
                item.getDiscountPercentage(),
                item.getLineTotal(),
                item.getNotes(),
                item.isRefunded(),
                item.getRefundedQuantity());
    }
}
