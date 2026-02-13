package com.gymmate.pos.domain;

/**
 * Status enumeration for POS sales transactions.
 */
public enum SaleStatus {
    PENDING, // Sale initiated but not completed
    COMPLETED, // Sale successfully completed
    REFUNDED, // Sale fully refunded
    PARTIALLY_REFUNDED, // Sale partially refunded
    CANCELLED, // Sale cancelled before completion
    VOID // Sale voided
}
