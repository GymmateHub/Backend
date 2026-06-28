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

    public static MemberInvoiceStatus fromProviderStatus(String providerStatus) {
        return switch (providerStatus) {
            case "draft" -> DRAFT;
            case "open" -> OPEN;
            case "paid" -> PAID;
            case "void" -> VOID;
            case "uncollectible" -> UNCOLLECTIBLE;
            default -> OPEN;
        };
    }
}

