package com.gymmate.billing.internal.domain;

import com.gymmate.shared.constants.InvoiceStatus;
import com.gymmate.shared.domain.BaseAuditEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an invoice for an organisation's platform subscription.
 * These are invoices from GymMate to the organisation (billing entity).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class GymInvoice extends BaseAuditEntity {

    /**
     * Organisation ID - the billing entity this invoice belongs to.
     * Primary filter for multi-tenant operations.
     */
    private UUID organisationId;

    private String stripeInvoiceId;

    private String invoiceNumber;

    private BigDecimal amount;

    @Builder.Default
    private 
    String currency = "USD";

    private 
    InvoiceStatus status;

    private String description;

    private LocalDateTime periodStart;

    private LocalDateTime periodEnd;

    private LocalDateTime dueDate;

    private LocalDateTime paidAt;

    private String invoicePdfUrl;

    private String hostedInvoiceUrl;

    public void markPaid(LocalDateTime paidAt) {
        this.status = InvoiceStatus.PAID;
        this.paidAt = paidAt;
    }

    public void markFailed() {
        this.status = InvoiceStatus.PAYMENT_FAILED;
    }

    public void markVoid() {
        this.status = InvoiceStatus.VOID;
    }
}
