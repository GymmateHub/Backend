package com.gymmate.scheduling.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

/**
 * GymArea entity representing a physical area within a gym.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class GymArea extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)

  private String name;

  private String areaType; // studio, pool, main_floor, outdoor, virtual

  private Integer capacity;

  private String[] amenities;

  // Booking rules
  @Builder.Default
  private boolean requiresBooking = false;

  @Builder.Default
  private Integer advanceBookingHours = 24;

  public void updateDetails(String name, String areaType, Integer capacity) {
    this.name = name;
    this.areaType = areaType;
    this.capacity = capacity;
  }
}
