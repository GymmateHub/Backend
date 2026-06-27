package com.gymmate.access.api.dto;

import com.gymmate.access.domain.AccessPoint;
import com.gymmate.access.domain.enums.AccessPointMode;
import com.gymmate.access.domain.enums.AccessPointType;

import java.util.UUID;

public record AccessPointResponse(
    UUID id,
    String name,
    AccessPointType type,
    AccessPointMode mode,
    UUID areaId,
    String deviceId,
    boolean online,
    Integer reentryLockoutSeconds,
    UUID gymId,
    UUID organisationId,
    boolean active
) {
  public static AccessPointResponse fromEntity(AccessPoint p) {
    return new AccessPointResponse(
        p.getId(), p.getName(), p.getType(), p.getMode(), p.getAreaId(), p.getDeviceId(),
        p.isOnline(), p.getReentryLockoutSeconds(), p.getGymId(), p.getOrganisationId(), p.isActive());
  }
}
