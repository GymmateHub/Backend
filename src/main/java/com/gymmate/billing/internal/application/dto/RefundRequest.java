package com.gymmate.billing.internal.application.dto;

import jakarta.validation.constraints.NotBlank;

import java.math.BigDecimal;

public record RefundRequest(
        @NotBlank(message = "Payment intent ID is required")
        String paymentIntentId, // Stripe payment intent ID
        BigDecimal amount, // null for full refund, in dollars
        String reason // optional reason for the refund
) {
}
