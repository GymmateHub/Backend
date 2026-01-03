package com.gymmate.classes.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * ClassCategory entity representing a category for classes.
 * Extends GymScopedEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "class_categories")
public class ClassCategory extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedEntity)

  @Column(nullable = false, length = 100)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(length = 7)
  private String color; // Hex color for UI

  @Column(length = 50)
  private String icon;

  public void updateDetails(String name, String description, String color) {
    this.name = name;
    this.description = description;
    this.color = color;
  }
}
