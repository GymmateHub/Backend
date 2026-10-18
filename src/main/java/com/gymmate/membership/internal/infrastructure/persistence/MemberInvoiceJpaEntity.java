package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.membership.internal.domain.MemberInvoice;
import com.gymmate.membership.internal.domain.MemberInvoiceStatus;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link MemberInvoice} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "MemberInvoice")
@Table(name = "member_invoices")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(MemberInvoice.class)
public class MemberInvoiceJpaEntity extends GymScopedJpaEntity {

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
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
}
