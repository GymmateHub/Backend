package com.gymmate.payment.domain;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Immutable audit log entry for refund-related actions.
 * Used for compliance and tracking.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@Builder
@Table(name = "refund_audit_log")
public class RefundAuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "refund_request_id")
    private UUID refundRequestId;

    @Column(name = "payment_refund_id")
    private UUID paymentRefundId;

    @Column(nullable = false, length = 50)
    private String action;

    @Column(name = "old_status", length = 30)
    private String oldStatus;

    @Column(name = "new_status", length = 30)
    private String newStatus;

    @Column(name = "performed_by_user_id")
    private UUID performedByUserId;

    @Column(name = "performed_by_type", length = 30)
    private String performedByType;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "ip_address", length = 45)
    private String ipAddress;

    @Column(name = "user_agent", columnDefinition = "TEXT")
    private String userAgent;

    @Column(columnDefinition = "TEXT")
    private String metadata;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    // ===== Factory Methods =====

    public static RefundAuditLog created(RefundRequestEntity request, UUID performedBy, String performedByType) {
        return RefundAuditLog.builder()
                .refundRequestId(request.getId())
                .action("CREATED")
                .newStatus(request.getStatus().name())
                .performedByUserId(performedBy)
                .performedByType(performedByType)
                .build();
    }

    public static RefundAuditLog statusChanged(RefundRequestEntity request, String oldStatus, UUID performedBy, String performedByType, String notes) {
        return RefundAuditLog.builder()
                .refundRequestId(request.getId())
                .action("STATUS_CHANGED")
                .oldStatus(oldStatus)
                .newStatus(request.getStatus().name())
                .performedByUserId(performedBy)
                .performedByType(performedByType)
                .notes(notes)
                .build();
    }

    public static RefundAuditLog approved(RefundRequestEntity request, UUID approvedBy, String approvedByType, String notes) {
        return RefundAuditLog.builder()
                .refundRequestId(request.getId())
                .action("APPROVED")
                .oldStatus(RefundRequestStatus.PENDING.name())
                .newStatus(RefundRequestStatus.APPROVED.name())
                .performedByUserId(approvedBy)
                .performedByType(approvedByType)
                .notes(notes)
                .build();
    }

    public static RefundAuditLog rejected(RefundRequestEntity request, UUID rejectedBy, String rejectedByType, String reason) {
        return RefundAuditLog.builder()
                .refundRequestId(request.getId())
                .action("REJECTED")
                .oldStatus(RefundRequestStatus.PENDING.name())
                .newStatus(RefundRequestStatus.REJECTED.name())
                .performedByUserId(rejectedBy)
                .performedByType(rejectedByType)
                .notes(reason)
                .build();
    }

    public static RefundAuditLog processed(RefundRequestEntity request, PaymentRefund refund, UUID processedBy, String processedByType) {
        return RefundAuditLog.builder()
                .refundRequestId(request.getId())
                .paymentRefundId(refund.getId())
                .action("PROCESSED")
                .oldStatus(RefundRequestStatus.APPROVED.name())
                .newStatus(RefundRequestStatus.PROCESSED.name())
                .performedByUserId(processedBy)
                .performedByType(processedByType)
                .notes("Stripe refund ID: " + refund.getStripeRefundId())
                .build();
    }

    public static RefundAuditLog escalated(RefundRequestEntity request, UUID escalatedBy, String escalatedByType, String escalatedTo) {
        return RefundAuditLog.builder()
                .refundRequestId(request.getId())
                .action("ESCALATED")
                .performedByUserId(escalatedBy)
                .performedByType(escalatedByType)
                .notes("Escalated to: " + escalatedTo)
                .build();
    }

    public static RefundAuditLog cancelled(RefundRequestEntity request, UUID cancelledBy, String cancelledByType) {
        return RefundAuditLog.builder()
                .refundRequestId(request.getId())
                .action("CANCELLED")
                .oldStatus(request.getStatus().name())
                .newStatus(RefundRequestStatus.CANCELLED.name())
                .performedByUserId(cancelledBy)
                .performedByType(cancelledByType)
                .build();
    }
}

