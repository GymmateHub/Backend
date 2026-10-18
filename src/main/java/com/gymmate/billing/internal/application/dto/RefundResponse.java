package com.gymmate.billing.internal.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record RefundResponse(
        String refundId,
        String paymentIntentId,
        BigDecimal amount,
        String currency,
        String status,
        String reason,
        LocalDateTime createdAt
) {
}
