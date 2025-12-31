package com.gymmate.payment.domain;

/**
 * Type of payment method owner.
 */
public enum PaymentMethodOwnerType {
    /**
     * Gym owner - for platform subscription payments (Gym → GymMate)
     */
    GYM,

    /**
     * Member - for gym service payments (Member → Gym)
     */
    MEMBER
}

