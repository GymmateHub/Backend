package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.constants.InvoiceStatus;
import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.billing.internal.domain.GymInvoice;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link GymInvoice} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "GymInvoice")
@Table(name = "gym_invoices", indexes = { @Index(name = "idx_gi_organisation", columnList = "organisation_id"), @Index(name = "idx_gi_stripe_invoice", columnList = "stripe_invoice_id") })
@Getter
@Setter
@NoArgsConstructor
@DomainModel(GymInvoice.class)
public class GymInvoiceJpaEntity extends BaseAuditJpaEntity {

    /**
     * Organisation ID - the billing entity this invoice belongs to.
     * Primary filter for multi-tenant operations.
     */
    @Column(name = "organisation_id")
    private UUID organisationId;

    @Column(name = "stripe_invoice_id", unique = true)
    private String stripeInvoiceId;

    @Column(name = "invoice_number", length = 50)
    private String invoiceNumber;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
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
}
