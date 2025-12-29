package com.gymmate.payment.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
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
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "refund_requests")
public class RefundRequestEntity extends BaseAuditEntity {

    @Column(name = "gym_id", nullable = false)
    private UUID gymId;

    @Enumerated(EnumType.STRING)
    @Column(name = "refund_type", nullable = false, length = 30)
    private RefundType refundType;

    // Payment Reference
    @Column(name = "stripe_payment_intent_id")
    private String stripePaymentIntentId;

    @Column(name = "stripe_charge_id")
    private String stripeChargeId;

    @Column(name = "original_payment_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal originalPaymentAmount;

    @Column(name = "requested_refund_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal requestedRefundAmount;

    @Column(length = 3)
    @Builder.Default
    private String currency = "USD";

    // Related Entities
    @Column(name = "membership_id")
    private UUID membershipId;

    @Column(name = "class_booking_id")
    private UUID classBookingId;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    // Requester Information
    @Column(name = "requested_by_user_id", nullable = false)
    private UUID requestedByUserId;

    @Column(name = "requested_by_type", nullable = false, length = 30)
    private String requestedByType; // MEMBER, GYM_OWNER, STAFF, SUPER_ADMIN

    // Recipient Information
    @Column(name = "refund_to_user_id", nullable = false)
    private UUID refundToUserId;

    @Column(name = "refund_to_type", nullable = false, length = 30)
    private String refundToType; // MEMBER, GYM_OWNER

    // Request Details
    @Enumerated(EnumType.STRING)
    @Column(name = "reason_category", nullable = false, length = 50)
    private RefundReasonCategory reasonCategory;

    @Column(name = "reason_description", columnDefinition = "TEXT")
    private String reasonDescription;

    @Column(name = "supporting_evidence", columnDefinition = "TEXT")
    private String supportingEvidence;

    // Workflow Status
    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private RefundRequestStatus status = RefundRequestStatus.PENDING;

    // Processor Information
    @Column(name = "processed_by_user_id")
    private UUID processedByUserId;

    @Column(name = "processed_by_type", length = 30)
    private String processedByType;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "processor_notes", columnDefinition = "TEXT")
    private String processorNotes;

    @Column(name = "rejection_reason", columnDefinition = "TEXT")
    private String rejectionReason;

    // Link to actual refund
    @Column(name = "payment_refund_id")
    private UUID paymentRefundId;

    // SLA Tracking
    @Column(name = "due_by")
    private LocalDateTime dueBy;

    @Builder.Default
    private Boolean escalated = false;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "escalated_to", length = 50)
    private String escalatedTo;

    @Column(columnDefinition = "TEXT")
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

