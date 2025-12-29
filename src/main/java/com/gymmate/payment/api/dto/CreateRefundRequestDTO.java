package com.gymmate.payment.api.dto;

import com.gymmate.payment.domain.RefundReasonCategory;
import com.gymmate.payment.domain.RefundType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * DTO for members/owners to submit refund requests.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateRefundRequestDTO {

    @NotNull(message = "Refund type is required")
    private RefundType refundType;

    @NotBlank(message = "Payment intent ID is required")
    private String stripePaymentIntentId;

    private String stripeChargeId;

    @NotNull(message = "Original payment amount is required")
    @DecimalMin(value = "0.01", message = "Original amount must be greater than 0")
    private BigDecimal originalPaymentAmount;

    @NotNull(message = "Requested refund amount is required")
    @DecimalMin(value = "0.01", message = "Refund amount must be greater than 0")
    private BigDecimal requestedRefundAmount;

    private String currency;

    // Related entities (optional, for context)
    private UUID membershipId;
    private UUID classBookingId;

    @NotNull(message = "Reason category is required")
    private RefundReasonCategory reasonCategory;

    private String reasonDescription;

    private String supportingEvidence; // URLs to uploaded files
}

