package com.gymmate.payment.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response containing invoice details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {

    private UUID id;
    private String invoiceNumber;
    private BigDecimal amount;
    private String currency;
    private String status;
    private String description;
    private LocalDateTime periodStart;
    private LocalDateTime periodEnd;
    private LocalDateTime dueDate;
    private LocalDateTime paidAt;
    private String invoicePdfUrl;
    private String hostedInvoiceUrl;
    private LocalDateTime createdAt;
}

