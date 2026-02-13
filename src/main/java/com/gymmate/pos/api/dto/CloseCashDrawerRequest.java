package com.gymmate.pos.api.dto;

import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for closing a cash drawer.
 */
public record CloseCashDrawerRequest(
        @NotNull(message = "Closing balance is required") @DecimalMin(value = "0.00", message = "Closing balance cannot be negative") BigDecimal closingBalance,

        String closingNotes) {
}
