package com.gymmate.billing.internal.domain;

import com.gymmate.shared.constants.RefundReasonCategory;
import com.gymmate.shared.constants.RefundRequestStatus;
import com.gymmate.shared.constants.RefundType;
import com.gymmate.shared.domain.BaseAuditEntity;
import com.gymmate.shared.domain.GymScopedEntity;
import com.gymmate.shared.domain.TenantEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Entity representing a refund request in the approval workflow.
 * Members can request refunds, gym owners/admins can approve/reject.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class RefundRequestEntity extends GymScopedEntity {

    private 
    RefundType refundType;

    // Payment Reference
    private String stripePaymentIntentId;

    private String stripeChargeId;

    private BigDecimal originalPaymentAmount;

    private BigDecimal requestedRefundAmount;

    @Builder.Default
    private 
    String currency = "USD";

    // Related Entities
    private UUID membershipId;

    private UUID classBookingId;

    private UUID subscriptionId;

    // Requester Information
    private UUID requestedByUserId;

    private String requestedByType; // MEMBER, GYM_OWNER, STAFF, SUPER_ADMIN

    // Recipient Information
    private UUID refundToUserId;

    private String refundToType; // MEMBER, GYM_OWNER

    // Request Details
    private 
    RefundReasonCategory reasonCategory;

    private String reasonDescription;

    private String supportingEvidence;

    // Workflow Status

    @Builder.Default
    private RefundRequestStatus status = RefundRequestStatus.PENDING;

    // Processor Information
    private UUID processedByUserId;

    private String processedByType;

    private LocalDateTime processedAt;

    private String processorNotes;

    private String rejectionReason;

    // Link to actual refund
    private UUID paymentRefundId;

    // SLA Tracking
    private LocalDateTime dueBy;

    @Builder.Default
    private Boolean escalated = false;

    private LocalDateTime escalatedAt;

    private String escalatedTo;

    private String metadata;

    // ===== Domain Methods =====

    /**
     * Mark the request as under review.
     */
    public void markUnderReview(UUID reviewerId, String reviewerType) {
        this.status = RefundRequestStatus.UNDER_REVIEW;
        this.processedByUserId = reviewerId;
        this.processedByType = reviewerType;
    }

    /**
     * Approve the refund request.
     */
    public void approve(UUID approverId, String approverType, String notes) {
        this.status = RefundRequestStatus.APPROVED;
        this.processedByUserId = approverId;
        this.processedByType = approverType;
        this.processedAt = LocalDateTime.now();
        this.processorNotes = notes;
    }

    /**
     * Reject the refund request.
     */
    public void reject(UUID rejecterId, String rejecterType, String reason, String notes) {
        this.status = RefundRequestStatus.REJECTED;
        this.processedByUserId = rejecterId;
        this.processedByType = rejecterType;
        this.processedAt = LocalDateTime.now();
        this.rejectionReason = reason;
        this.processorNotes = notes;
    }

    /**
     * Mark the request as processed after successful Stripe refund.
     */
    public void markProcessed(UUID paymentRefundId) {
        this.status = RefundRequestStatus.PROCESSED;
        this.paymentRefundId = paymentRefundId;
        if (this.processedAt == null) {
            this.processedAt = LocalDateTime.now();
        }
    }

    /**
     * Cancel the refund request.
     */
    public void cancel() {
        this.status = RefundRequestStatus.CANCELLED;
    }

    /**
     * Escalate the request for higher-level review.
     */
    public void escalate(String escalateTo) {
        this.escalated = true;
        this.escalatedAt = LocalDateTime.now();
        this.escalatedTo = escalateTo;
    }

    /**
     * Check if the request can be approved.
     */
    public boolean canBeApproved() {
        return status == RefundRequestStatus.PENDING || status == RefundRequestStatus.UNDER_REVIEW;
    }

    /**
     * Check if the request can be cancelled.
     */
    public boolean canBeCancelled() {
        return status == RefundRequestStatus.PENDING || status == RefundRequestStatus.UNDER_REVIEW;
    }
}
