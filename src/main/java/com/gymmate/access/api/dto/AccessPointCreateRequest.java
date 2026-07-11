package com.gymmate.access.api.dto;

import com.gymmate.access.domain.enums.AccessPointMode;
import com.gymmate.access.domain.enums.AccessPointType;
import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record AccessPointCreateRequest(
    @NotBlank String name,
    AccessPointType type,
    AccessPointMode mode,
    UUID areaId,
    String deviceId,
    Integer reentryLockoutSeconds,
    UUID gymId
) {
}
