package com.gymmate.payment.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a payment refund for audit and analytics tracking.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "payment_refunds", indexes = {
    @Index(name = "idx_pr_organisation", columnList = "organisation_id"),
    @Index(name = "idx_pr_gym", columnList = "gym_id"),
    @Index(name = "idx_pr_stripe_refund", columnList = "stripe_refund_id")
})
public class PaymentRefund extends BaseAuditEntity {

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
    @Builder.Default
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
    @Builder.Default
    private RefundType refundType = RefundType.PLATFORM_SUBSCRIPTION;

    // Refund Recipient (who gets the money)
    @Column(name = "refund_to_user_id")
    private UUID refundToUserId;

    @Column(name = "refund_to_type", length = 30)
    private String refundToType; // MEMBER, GYM_OWNER

    // Who requested the refund
    @Column(name = "requested_by")
    private UUID requestedBy;

    @Column(name = "requested_by_type", length = 20)
    @Builder.Default
    private String requestedByType = "user";

    // Who processed/approved the refund
    @Column(name = "processed_by_user_id")
    private UUID processedByUserId;

    @Column(name = "processed_by_type", length = 30)
    private String processedByType; // GYM_OWNER, SUPER_ADMIN, SYSTEM

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

    /**
     * Update the status of the refund.
     */
    public void updateStatus(RefundStatus newStatus) {
        this.status = newStatus;
    }

    /**
     * Mark the refund as failed with a reason.
     */
    public void markFailed(String failureReason) {
        this.status = RefundStatus.FAILED;
        this.failureReason = failureReason;
    }
}

