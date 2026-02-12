package com.gymmate.pos.domain;

/**
 * Payment type enumeration for POS transactions.
 */
public enum PaymentType {
    CASH,
    CARD,
    STRIPE,
    MEMBER_ACCOUNT, // Charge to member's account/tab
    SPLIT, // Multiple payment methods
    GIFT_VOUCHER,
    CREDIT_NOTE
}
