package com.gymmate.scheduling.internal.domain;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;

/**
 * ClassCategory entity representing a category for classes.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "class_categories")
public class ClassCategory extends GymScopedJpaEntity {

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)

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
