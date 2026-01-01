package com.gymmate.classes.domain;

import com.gymmate.shared.domain.TenantEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "gym_areas")
public class GymArea extends TenantEntity {

  @Column(name = "gym_id", nullable = false)
  private UUID gymId;

  @Column(nullable = false, length = 100)
  private String name;

  @Column(name = "area_type", length = 50)
  private String areaType; // studio, pool, main_floor, outdoor, virtual

  @Column
  private Integer capacity;

  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(columnDefinition = "text[]")
  private String[] amenities;

  // Booking rules
  @Column(name = "requires_booking")
  @Builder.Default
  private boolean requiresBooking = false;

  @Column(name = "advance_booking_hours")
  @Builder.Default
  private Integer advanceBookingHours = 24;

  public void updateDetails(String name, String areaType, Integer capacity) {
    this.name = name;
    this.areaType = areaType;
    this.capacity = capacity;
  }
}

