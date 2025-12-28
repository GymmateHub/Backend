package com.gymmate.payment.domain;

/**
 * Status of an invoice.
 */
public enum InvoiceStatus {
    DRAFT,
    OPEN,
    PAID,
    PAYMENT_FAILED,
    VOID,
    UNCOLLECTIBLE;

    public static InvoiceStatus fromStripeStatus(String stripeStatus) {
        return switch (stripeStatus) {
            case "draft" -> DRAFT;
            case "open" -> OPEN;
            case "paid" -> PAID;
            case "void" -> VOID;
            case "uncollectible" -> UNCOLLECTIBLE;
            default -> OPEN;
        };
    }
}

