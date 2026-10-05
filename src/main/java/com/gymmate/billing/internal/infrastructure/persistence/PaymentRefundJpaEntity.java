package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.constants.RefundStatus;
import com.gymmate.shared.constants.RefundType;
import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.billing.internal.domain.PaymentRefund;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link PaymentRefund} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "PaymentRefund")
@Table(name = "payment_refunds", indexes = { @Index(name = "idx_pr_organisation", columnList = "organisation_id"), @Index(name = "idx_pr_gym", columnList = "gym_id"), @Index(name = "idx_pr_stripe_refund", columnList = "stripe_refund_id") })
@Getter
@Setter
@NoArgsConstructor
@DomainModel(PaymentRefund.class)
public class PaymentRefundJpaEntity extends BaseAuditJpaEntity {

    /**
     * Organisation ID - the billing entity this refund belongs to.
     * Primary filter for multi-tenant operations.
     */
    @Column(name = "organisation_id")
    private UUID organisationId;

    @Column(name = "gym_id")
    private UUID gymId;

    @Column(name = "stripe_refund_id", unique = true, nullable = false)
    private String stripeRefundId;

    @Column(name = "stripe_payment_intent_id", nullable = false)
    private String stripePaymentIntentId;

    @Column(name = "stripe_charge_id")
    private String stripeChargeId;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(length = 3)
    private String currency = "USD";

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private RefundStatus status;

    @Column(length = 50)
    private String reason;

    @Column(name = "custom_reason", columnDefinition = "TEXT")
    private String customReason;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "invoice_id")
    private UUID invoiceId;

    // Refund Type
    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", length = 30)
    private RefundType refundType = RefundType.PLATFORM_SUBSCRIPTION;

    // Refund Recipient (who gets the money)
    @Column(name = "refund_to_user_id")
    private UUID refundToUserId;

    @Column(name = "refund_to_type", length = 30)
    private String // MEMBER, GYM_OWNER
    refundToType;

    // Who requested the refund
    @Column(name = "requested_by")
    private UUID requestedBy;

    @Column(name = "requested_by_type", length = 20)
    private String requestedByType = "user";

    // Who processed/approved the refund
    @Column(name = "processed_by_user_id")
    private UUID processedByUserId;

    @Column(name = "processed_by_type", length = 30)
    private String // GYM_OWNER, SUPER_ADMIN, SYSTEM
    processedByType;

    // Link to refund request (if workflow was used)
    @Column(name = "refund_request_id")
    private UUID refundRequestId;

    @Column(name = "failure_reason")
    private String failureReason;

    @Column(name = "receipt_number")
    private String receiptNumber;

    @Column(name = "stripe_created_at")
    private LocalDateTime stripeCreatedAt;

    @Column(columnDefinition = "TEXT")
    private String metadata;
}
