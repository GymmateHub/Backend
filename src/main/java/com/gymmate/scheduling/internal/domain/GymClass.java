package com.gymmate.scheduling.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * GymClass entity representing a class type offered at a gym.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class GymClass extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
  private UUID categoryId;

  private String name;

  private String description;

  private Integer durationMinutes;

  @Builder.Default
  private 
  Integer capacity = 20;

  // Pricing
  @Builder.Default
  private 
  BigDecimal price = BigDecimal.ZERO;

  @Builder.Default
  private 
  Integer creditsRequired = 1;

  // Requirements
  private String skillLevel; // beginner, intermediate, advanced, all_levels

  private String ageRestriction; // "18+", "16+", "all_ages"

  private 
  String[] equipmentNeeded;

  // Content
  private String imageUrl;

  private String videoUrl;

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
