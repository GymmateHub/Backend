package com.gymmate.payment.api.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequest {
    @NotBlank(message = "Payment intent ID is required")
    private String paymentIntentId; // Stripe payment intent ID

    private BigDecimal amount; // null for full refund, in dollars

    private String reason; // optional reason for the refund
}
