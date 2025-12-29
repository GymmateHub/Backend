package com.gymmate.payment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundResponse {
    private String refundId;
    private String paymentIntentId;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String reason;
    private LocalDateTime createdAt;
}

