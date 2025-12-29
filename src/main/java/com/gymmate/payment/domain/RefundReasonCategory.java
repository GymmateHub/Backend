package com.gymmate.payment.domain;

/**
 * Reason categories for refund requests.
 */
public enum RefundReasonCategory {
    SERVICE_NOT_PROVIDED,
    DUPLICATE_CHARGE,
    DISSATISFIED,
    CANCELLED_MEMBERSHIP,
    CLASS_CANCELLED,
    TRAINER_UNAVAILABLE,
    BILLING_ERROR,
    FRAUDULENT_CHARGE,
    OTHER
}

