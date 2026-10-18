package com.gymmate.scheduling.internal.application.dto;

import com.gymmate.scheduling.internal.domain.GymArea;

import java.util.UUID;

public record AreaResponse(
    UUID id,
    UUID gymId,
    String name,
    String areaType,
    Integer capacity
) {

  public static AreaResponse from(GymArea a) {
    return new AreaResponse(
        a.getId(),
        a.getGymId(),
        a.getName(),
        a.getAreaType(),
        a.getCapacity());
  }
}
