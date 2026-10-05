package com.gymmate.billing.internal.domain;

import com.gymmate.shared.constants.RefundStatus;
import com.gymmate.shared.constants.RefundType;
import com.gymmate.shared.domain.BaseAuditEntity;
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
@EqualsAndHashCode(callSuper = true)
@Builder
public class PaymentRefund extends BaseAuditEntity {

    /**
     * Organisation ID - the billing entity this refund belongs to.
     * Primary filter for multi-tenant operations.
     */
    private UUID organisationId;

    private UUID gymId;

    private String stripeRefundId;

    private String stripePaymentIntentId;

    private String stripeChargeId;

    private BigDecimal amount;

    @Builder.Default
    private 
    String currency = "USD";

    private 
    RefundStatus status;

    private String reason;

    private String customReason;

    private UUID subscriptionId;

    private UUID invoiceId;

    // Refund Type

    @Builder.Default
    private RefundType refundType = RefundType.PLATFORM_SUBSCRIPTION;

    // Refund Recipient (who gets the money)
    private UUID refundToUserId;

    private String refundToType; // MEMBER, GYM_OWNER

    // Who requested the refund
    private UUID requestedBy;

    @Builder.Default
    private 
    String requestedByType = "user";

    // Who processed/approved the refund
    private UUID processedByUserId;

    private String processedByType; // GYM_OWNER, SUPER_ADMIN, SYSTEM

    // Link to refund request (if workflow was used)
    private UUID refundRequestId;

    private String failureReason;

    private String receiptNumber;

    private LocalDateTime stripeCreatedAt;

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
