package com.gymmate.membership.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing an invoice for a member's membership.
 * These are invoices from the gym to the member.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class MemberInvoice extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
    private UUID memberId;

    private UUID membershipId;

    private String stripeInvoiceId;

    private String invoiceNumber;

    private BigDecimal amount;

    @Builder.Default
    private String currency = "USD";

    private MemberInvoiceStatus status;

    private String description;

    private LocalDateTime periodStart;

    private LocalDateTime periodEnd;

    private LocalDateTime dueDate;

    private LocalDateTime paidAt;

    private String invoicePdfUrl;

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
