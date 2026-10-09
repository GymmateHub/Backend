package com.gymmate.billing.internal.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response containing invoice details.
 */
public record InvoiceResponse(
        UUID id,
        String invoiceNumber,
        BigDecimal amount,
        String currency,
        String status,
        String description,
        LocalDateTime periodStart,
        LocalDateTime periodEnd,
        LocalDateTime dueDate,
        LocalDateTime paidAt,
        String invoicePdfUrl,
        String hostedInvoiceUrl,
        LocalDateTime createdAt
) {
}
