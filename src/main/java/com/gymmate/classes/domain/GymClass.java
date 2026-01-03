package com.gymmate.classes.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * GymClass entity representing a class type offered at a gym.
 * Extends GymScopedEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "classes")
public class GymClass extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedEntity)
  @Column(name = "category_id")
  private UUID categoryId;

  @Column(nullable = false)
  private String name;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "duration_minutes", nullable = false)
  private Integer durationMinutes;

  @Column
  @Builder.Default
  private Integer capacity = 20;

  // Pricing
  @Column(precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal price = BigDecimal.ZERO;

  @Column(name = "credits_required")
  @Builder.Default
  private Integer creditsRequired = 1;

  // Requirements
  @Column(name = "skill_level", length = 20)
  private String skillLevel; // beginner, intermediate, advanced, all_levels

  @Column(name = "age_restriction", length = 50)
  private String ageRestriction; // "18+", "16+", "all_ages"

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(name = "equipment_needed", columnDefinition = "text[]")
  private String[] equipmentNeeded;

  // Content
  @Column(name = "image_url", length = 500)
  private String imageUrl;

  @Column(name = "video_url", length = 500)
  private String videoUrl;

  @Column(columnDefinition = "TEXT")
  private String instructions;

  public void updateDetails(String name, String description, Integer durationMinutes) {
    this.name = name;
    this.description = description;
    this.durationMinutes = durationMinutes;
  }

  public void updatePricing(BigDecimal price, Integer creditsRequired) {
    this.price = price;
    this.creditsRequired = creditsRequired;
  }

  public void updateCapacity(Integer capacity) {
    this.capacity = capacity;
  }
}
