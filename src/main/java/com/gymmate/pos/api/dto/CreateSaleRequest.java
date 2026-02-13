package com.gymmate.pos.api.dto;

import com.gymmate.pos.domain.PaymentType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

/**
 * Request DTO for creating a new sale.
 */
public record CreateSaleRequest(
        @NotNull(message = "Gym ID is required") UUID gymId,

        UUID memberId, // Optional for member sales

        String customerName, // For walk-in customers

        UUID staffId,

        @NotEmpty(message = "At least one item is required") @Valid List<SaleItemRequest> items,

        @NotNull(message = "Payment type is required") PaymentType paymentType,

        @DecimalMin(value = "0.00", message = "Discount percentage cannot be negative") @DecimalMax(value = "100.00", message = "Discount percentage cannot exceed 100%") BigDecimal discountPercentage,

        String discountCode,

        BigDecimal taxRate,

        BigDecimal amountPaid, // For cash transactions

        String notes) {
}
