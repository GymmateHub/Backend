package com.gymmate.payment.api.dto;

import com.gymmate.payment.domain.RefundReasonCategory;
import com.gymmate.payment.domain.RefundRequestStatus;
import com.gymmate.payment.domain.RefundType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for refund request details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RefundRequestResponse {

    private UUID id;
    private UUID gymId;
    private RefundType refundType;

    // Payment info
    private String stripePaymentIntentId;
    private BigDecimal originalPaymentAmount;
    private BigDecimal requestedRefundAmount;
    private String currency;

    // Related entities
    private UUID membershipId;
    private UUID classBookingId;

    // Requester info
    private UUID requestedByUserId;
    private String requestedByType;
    private String requestedByName; // Populated from user service

    // Recipient info
    private UUID refundToUserId;
    private String refundToType;
    private String refundToName; // Populated from user service

    // Request details
    private RefundReasonCategory reasonCategory;
    private String reasonDescription;

    // Status
    private RefundRequestStatus status;
    private String rejectionReason;
    private String processorNotes;

    // Processor info
    private UUID processedByUserId;
    private String processedByType;
    private String processedByName;
    private LocalDateTime processedAt;

    // SLA
    private LocalDateTime dueBy;
    private Boolean escalated;
    private String escalatedTo;

    // Link to actual refund
    private UUID paymentRefundId;
    private String stripeRefundId;

    // Timestamps
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}

