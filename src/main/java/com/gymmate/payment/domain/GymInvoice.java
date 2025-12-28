package com.gymmate.payment.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an invoice for a gym's platform subscription.
 * These are invoices from GymMate to the gym.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "gym_invoices")
public class GymInvoice extends BaseAuditEntity {

    @Column(name = "gym_id", nullable = false)
    private UUID gymId;

    @Column(name = "stripe_invoice_id", unique = true)
    private String stripeInvoiceId;

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private InvoiceStatus status;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "period_start")
    private LocalDateTime periodStart;

    @Column(name = "period_end")
    private LocalDateTime periodEnd;

    @Column(name = "due_date")
    private LocalDateTime dueDate;

    @Column(name = "paid_at")
    private LocalDateTime paidAt;

    @Column(name = "invoice_pdf_url", columnDefinition = "TEXT")
    private String invoicePdfUrl;

    @Column(name = "hosted_invoice_url", columnDefinition = "TEXT")
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

