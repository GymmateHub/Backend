package com.gymmate.shared.constants;

/**
 * Status of an invoice.
 */
public enum InvoiceStatus {
    DRAFT,
    OPEN,
    PAID,
    PAYMENT_FAILED,
    VOID,
    UNCOLLECTIBLE,
    REFUNDED;

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

