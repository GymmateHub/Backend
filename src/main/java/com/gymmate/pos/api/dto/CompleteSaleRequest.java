package com.gymmate.pos.api.dto;

import com.gymmate.pos.domain.PaymentType;
import jakarta.validation.constraints.*;

import java.math.BigDecimal;

/**
 * Request DTO for completing a sale payment.
 */
public record CompleteSaleRequest(
        @NotNull(message = "Payment type is required") PaymentType paymentType,

        @NotNull(message = "Amount paid is required") @DecimalMin(value = "0.00", message = "Amount paid cannot be negative") BigDecimal amountPaid,

        String stripePaymentIntentId,

        String externalReference) {
}
