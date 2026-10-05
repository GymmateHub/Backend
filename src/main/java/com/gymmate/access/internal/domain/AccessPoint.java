package com.gymmate.access.internal.domain;

import com.gymmate.access.internal.domain.enums.AccessPointMode;
import com.gymmate.access.internal.domain.enums.AccessPointType;
import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.util.UUID;

/**
 * A controlled physical entry point (door / turnstile / gate) at a gym.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class AccessPoint extends GymScopedEntity {

  private String name;

  @Builder.Default
  private AccessPointType type = AccessPointType.MAIN_DOOR;

  @Builder.Default
  private AccessPointMode mode = AccessPointMode.SOFTWARE;

  /** Optional link to a {@code GymArea} this point guards. */
  private UUID areaId;

  /** Hardware device identifier (for TURNSTILE/CV modes). */
  private String deviceId;

  @Builder.Default
  private 
  boolean online = true;

  /** Cooldown before the same credential may grant entry again (pass-back defence). */
  @Builder.Default
  private 
  Integer reentryLockoutSeconds = 300;
}
