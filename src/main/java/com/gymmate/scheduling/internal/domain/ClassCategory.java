package com.gymmate.scheduling.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

/**
 * ClassCategory entity representing a category for classes.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class ClassCategory extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)

  private String name;

  private String description;

  private String color; // Hex color for UI

  private String icon;

  public void updateDetails(String name, String description, String color) {
    this.name = name;
    this.description = description;
    this.color = color;
  }
}
