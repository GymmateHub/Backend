package com.gymmate.payment.domain;

/**
 * Type of payment method.
 */
public enum PaymentMethodType {
    /**
     * Credit/Debit card
     */
    CARD,

    /**
     * Bank account (ACH, SEPA, etc.)
     */
    BANK_ACCOUNT,

    /**
     * Digital wallet (Apple Pay, Google Pay, PayPal, etc.)
     */
    DIGITAL_WALLET,

    /**
     * Other payment methods
     */
    OTHER;

    /**
     * Map Stripe payment method type to our enum.
     */
    public static PaymentMethodType fromStripeType(String stripeType) {
        if (stripeType == null) {
            return OTHER;
        }
        return switch (stripeType.toLowerCase()) {
            case "card" -> CARD;
            case "us_bank_account", "sepa_debit", "bacs_debit", "acss_debit" -> BANK_ACCOUNT;
            case "apple_pay", "google_pay", "paypal", "link" -> DIGITAL_WALLET;
            default -> OTHER;
        };
    }
}

