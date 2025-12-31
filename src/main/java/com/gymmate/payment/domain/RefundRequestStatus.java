package com.gymmate.payment.domain;

/**
 * Status of a refund request in the approval workflow.
 */
public enum RefundRequestStatus {
    /**
     * Request submitted, awaiting review.
     */
    PENDING,

    /**
     * Request is being reviewed by processor.
     */
    UNDER_REVIEW,

    /**
     * Request approved, ready to process refund.
     */
    APPROVED,

    /**
     * Request denied by processor.
     */
    REJECTED,

    /**
     * Refund has been processed via Stripe.
     */
    PROCESSED,

    /**
     * Request was cancelled/withdrawn by requester.
     */
    CANCELLED
}

