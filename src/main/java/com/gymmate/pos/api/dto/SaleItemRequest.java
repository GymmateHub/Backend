package com.gymmate.pos.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for individual sale items.
 */
public record SaleItemRequest(
        UUID inventoryItemId,

        @NotBlank(message = "Item name is required") String itemName,

        String itemSku,

        String itemBarcode,

        @NotNull(message = "Quantity is required") @Min(value = 1, message = "Quantity must be at least 1") Integer quantity,

        @NotNull(message = "Unit price is required") @DecimalMin(value = "0.00", message = "Unit price cannot be negative") BigDecimal unitPrice,

        BigDecimal costPrice,

        @DecimalMin(value = "0.00", message = "Discount percentage cannot be negative") @DecimalMax(value = "100.00", message = "Discount percentage cannot exceed 100%") BigDecimal discountPercentage,

        String notes) {
}
