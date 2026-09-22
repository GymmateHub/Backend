package com.gymmate.inventory.internal.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

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
  DAMAGED;          // Damaged, needs repair or disposal

  @JsonCreator
  public static EquipmentStatus fromString(String value) {
    if (value == null || value.isBlank()) return AVAILABLE;
    for (EquipmentStatus st : values()) {
      if (st.name().equalsIgnoreCase(value.trim()) || (value.equalsIgnoreCase("OPERATIONAL") && st == AVAILABLE)) {
        return st;
      }
    }
    return AVAILABLE;
  }
}
