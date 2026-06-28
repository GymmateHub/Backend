package com.gymmate.access.domain;

import com.gymmate.access.domain.enums.AccessPointMode;
import com.gymmate.access.domain.enums.AccessPointType;
import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * A controlled physical entry point (door / turnstile / gate) at a gym.
 * Extends GymScopedEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "access_points")
public class AccessPoint extends GymScopedEntity {

  @Column(nullable = false, length = 100)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private AccessPointType type = AccessPointType.MAIN_DOOR;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private AccessPointMode mode = AccessPointMode.SOFTWARE;

  /** Optional link to a {@code GymArea} this point guards. */
  @Column(name = "area_id")
  private UUID areaId;

  /** Hardware device identifier (for TURNSTILE/CV modes). */
  @Column(name = "device_id", length = 100)
  private String deviceId;

  @Column(name = "online")
  @Builder.Default
  private boolean online = true;

  /** Cooldown before the same credential may grant entry again (pass-back defence). */
  @Column(name = "reentry_lockout_seconds")
  @Builder.Default
  private Integer reentryLockoutSeconds = 300;
}
