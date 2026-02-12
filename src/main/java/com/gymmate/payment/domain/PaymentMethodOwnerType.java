package com.gymmate.payment.domain;

/**
 * Type of payment method owner.
 */
public enum PaymentMethodOwnerType {
    /**
     * Organisation - for platform subscription payments (Organisation → GymMate)
     * This is the primary owner type for billing purposes.
     */
    ORGANISATION,

    /**
     * Member - for gym service payments (Member → Gym)
     */
    MEMBER
}
