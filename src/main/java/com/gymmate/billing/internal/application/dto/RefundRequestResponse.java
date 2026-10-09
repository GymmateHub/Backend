package com.gymmate.billing.internal.application.dto;

import com.gymmate.shared.constants.RefundReasonCategory;
import com.gymmate.shared.constants.RefundRequestStatus;
import com.gymmate.shared.constants.RefundType;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for refund request details.
 */
public record RefundRequestResponse(
        UUID id,
        UUID gymId,
        RefundType refundType,
        // Payment info
        String stripePaymentIntentId,
        BigDecimal originalPaymentAmount,
        BigDecimal requestedRefundAmount,
        String currency,
        // Related entities
        UUID membershipId,
        UUID classBookingId,
        // Requester info
        UUID requestedByUserId,
        String requestedByType,
        String requestedByName, // Populated from user service
        // Recipient info
        UUID refundToUserId,
        String refundToType,
        String refundToName, // Populated from user service
        // Request details
        RefundReasonCategory reasonCategory,
        String reasonDescription,
        // Status
        RefundRequestStatus status,
        String rejectionReason,
        String processorNotes,
        // Processor info
        UUID processedByUserId,
        String processedByType,
        String processedByName,
        LocalDateTime processedAt,
        // SLA
        LocalDateTime dueBy,
        Boolean escalated,
        String escalatedTo,
        // Link to actual refund
        UUID paymentRefundId,
        String stripeRefundId,
        // Timestamps
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
}
