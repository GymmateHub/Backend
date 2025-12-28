package com.gymmate.membership.domain;

import com.gymmate.shared.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an invoice for a member's membership.
 * These are invoices from the gym to the member.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "member_invoices")
public class MemberInvoice extends TenantEntity {

    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "membership_id")
    private UUID membershipId;

    @Column(name = "stripe_invoice_id")
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
    private MemberInvoiceStatus status;

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
        this.status = MemberInvoiceStatus.PAID;
        this.paidAt = paidAt;
    }

    public void markFailed() {
        this.status = MemberInvoiceStatus.PAYMENT_FAILED;
    }

    public void markVoid() {
        this.status = MemberInvoiceStatus.VOID;
    }
}

