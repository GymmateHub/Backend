package com.gymmate.membership.domain;

/**
 * Status of a member invoice.
 */
public enum MemberInvoiceStatus {
    DRAFT,
    OPEN,
    PAID,
    PAYMENT_FAILED,
    VOID,
    UNCOLLECTIBLE;

    public static MemberInvoiceStatus fromStripeStatus(String stripeStatus) {
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

