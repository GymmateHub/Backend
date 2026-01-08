package com.gymmate.inventory.domain;

/**
 * Stock movement type enumeration.
 * Tracks the type of inventory transaction.
 */
public enum MovementType {
  PURCHASE,         // Stock purchased from supplier
  SALE,             // Stock sold to customer
  ADJUSTMENT,       // Manual stock adjustment
  DAMAGE,           // Stock damaged/written off
  RETURN,           // Stock returned by customer
  TRANSFER_IN,      // Stock transferred from another location
  TRANSFER_OUT,     // Stock transferred to another location
  INITIAL_STOCK     // Initial stock entry
}
