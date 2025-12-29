package com.gymmate.payment.domain;

/**
 * Status of a payment refund.
 */
public enum RefundStatus {
    PENDING,
    SUCCEEDED,
    FAILED,
    CANCELED,
    REQUIRES_ACTION;

    /**
     * Convert Stripe refund status to RefundStatus enum.
     */
    public static RefundStatus fromStripeStatus(String stripeStatus) {
        if (stripeStatus == null) {
            return PENDING;
        }
        return switch (stripeStatus.toLowerCase()) {
            case "succeeded" -> SUCCEEDED;
            case "failed" -> FAILED;
            case "canceled" -> CANCELED;
            case "requires_action" -> REQUIRES_ACTION;
            default -> PENDING;
        };
    }
}

