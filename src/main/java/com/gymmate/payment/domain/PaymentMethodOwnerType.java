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
     * Gym owner - for platform subscription payments (Gym → GymMate)
     * @deprecated Use ORGANISATION instead for new implementations.
     */
    @Deprecated(since = "1.0", forRemoval = true)
    GYM,

    /**
     * Member - for gym service payments (Member → Gym)
     */
    MEMBER
}

