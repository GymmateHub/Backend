package com.gymmate.inventory.internal.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Equipment category enumeration.
 * Categorizes gym equipment by type.
 */
public enum EquipmentCategory {
  CARDIO,           // Treadmills, bikes, ellipticals
  STRENGTH,         // Weight machines, free weights
  FUNCTIONAL,       // Kettlebells, resistance bands, TRX
  BOXING,           // Heavy bags, speed bags, gloves
  YOGA,             // Mats, blocks, straps
  SWIMMING,         // Pool equipment, accessories
  SPORTS,           // Basketballs, soccer balls, etc.
  ACCESSIBILITY,    // Wheelchairs, adaptive equipment
  RECOVERY,         // Foam rollers, massage guns
  OTHER;            // Miscellaneous equipment

  @JsonCreator
  public static EquipmentCategory fromString(String value) {
    if (value == null || value.isBlank()) return OTHER;
    for (EquipmentCategory cat : values()) {
      if (cat.name().equalsIgnoreCase(value.trim())) {
        return cat;
      }
    }
    return OTHER;
  }
}
