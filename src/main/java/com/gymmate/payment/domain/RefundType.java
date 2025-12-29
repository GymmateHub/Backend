package com.gymmate.payment.domain;

/**
 * Type of refund based on payment source.
 */
public enum RefundType {
    /**
     * Refund for gym owner's platform subscription payment (Gym → GymMate).
     * Processed by SUPER_ADMIN.
     */
    PLATFORM_SUBSCRIPTION,

    /**
     * Refund for member's payment to the gym (Member → Gym).
     * Processed by GYM_OWNER.
     */
    MEMBER_PAYMENT
}

