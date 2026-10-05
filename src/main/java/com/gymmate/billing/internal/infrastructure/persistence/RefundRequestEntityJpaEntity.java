package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.constants.RefundReasonCategory;
import com.gymmate.shared.constants.RefundRequestStatus;
import com.gymmate.shared.constants.RefundType;
import com.gymmate.shared.infrastructure.persistence.BaseAuditJpaEntity;
import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import com.gymmate.shared.infrastructure.persistence.TenantJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.billing.internal.domain.RefundRequestEntity;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link RefundRequestEntity} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "RefundRequestEntity")
@Table(name = "refund_requests")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(RefundRequestEntity.class)
public class // ===== Domain Methods =====
RefundRequestEntityJpaEntity extends GymScopedJpaEntity {

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
    private String // MEMBER, GYM_OWNER, STAFF, SUPER_ADMIN
    requestedByType;

    // Recipient Information
    @Column(name = "refund_to_user_id", nullable = false)
    private UUID refundToUserId;

    @Column(name = "refund_to_type", nullable = false, length = 30)
    private String // MEMBER, GYM_OWNER
    refundToType;

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

    private Boolean escalated = false;

    @Column(name = "escalated_at")
    private LocalDateTime escalatedAt;

    @Column(name = "escalated_to", length = 50)
    private String escalatedTo;

    @Column(columnDefinition = "TEXT")
    private String metadata;
}
