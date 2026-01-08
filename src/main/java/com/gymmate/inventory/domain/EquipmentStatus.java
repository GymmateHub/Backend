package com.gymmate.inventory.domain;

/**
 * Equipment status enumeration.
 * Tracks the current state of gym equipment.
 */
public enum EquipmentStatus {
  AVAILABLE,        // Ready for use
  IN_USE,           // Currently being used
  MAINTENANCE,      // Under maintenance/repair
  RETIRED,          // No longer in service
  ORDERED,          // Ordered but not yet received
  DAMAGED           // Damaged, needs repair or disposal
}
