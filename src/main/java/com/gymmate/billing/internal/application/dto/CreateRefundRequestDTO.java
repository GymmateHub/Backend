package com.gymmate.billing.internal.application.dto;

import com.gymmate.shared.constants.RefundReasonCategory;
import com.gymmate.shared.constants.RefundType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for members/owners to submit refund requests.
 */
public record CreateRefundRequestDTO(
        @NotNull(message = "Refund type is required")
        RefundType refundType,
        @NotBlank(message = "Payment intent ID is required")
        String stripePaymentIntentId,
        String stripeChargeId,
        @NotNull(message = "Original payment amount is required")
        @DecimalMin(value = "0.01", message = "Original amount must be greater than 0")
        BigDecimal originalPaymentAmount,
        @NotNull(message = "Requested refund amount is required")
        @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
        BigDecimal requestedRefundAmount,
        String currency,
        // Related entities (optional, for context)
        UUID membershipId,
        UUID classBookingId,
        @NotNull(message = "Reason category is required")
        RefundReasonCategory reasonCategory,
        String reasonDescription,
        String supportingEvidence // URLs to uploaded files
) {

    /** This request with its refund type replaced. */
    public CreateRefundRequestDTO withRefundType(RefundType refundType) {
        return new CreateRefundRequestDTO(refundType, stripePaymentIntentId, stripeChargeId, originalPaymentAmount, requestedRefundAmount, currency, membershipId, classBookingId, reasonCategory, reasonDescription, supportingEvidence);
    }
}
