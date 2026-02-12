package com.gymmate.pos.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * Request DTO for opening a cash drawer.
 */
public record OpenCashDrawerRequest(
        @NotNull(message = "Gym ID is required") UUID gymId,

        @NotNull(message = "Opening balance is required") @DecimalMin(value = "0.00", message = "Opening balance cannot be negative") BigDecimal openingBalance,

        String notes) {
}
