package com.gymmate.inventory.internal.domain;

import com.fasterxml.jackson.annotation.JsonCreator;

/**
 * Inventory item category enumeration.
 * Categorizes retail and supply inventory items.
 */
public enum InventoryCategory {
  SUPPLEMENTS,      // Protein, vitamins, pre-workout
  APPAREL,          // Clothing, shoes, accessories
  ACCESSORIES,      // Water bottles, bags, towels
  SUPPLIES,         // Cleaning supplies, office supplies
  MERCHANDISE,      // Branded items, gifts
  EQUIPMENT_PARTS,  // Replacement parts for equipment
  OTHER;            // Miscellaneous items

  @JsonCreator
  public static InventoryCategory fromString(String value) {
    if (value == null || value.isBlank()) return OTHER;
    for (InventoryCategory cat : values()) {
      if (cat.name().equalsIgnoreCase(value.trim())) {
        return cat;
      }
    }
    return OTHER;
  }
}
