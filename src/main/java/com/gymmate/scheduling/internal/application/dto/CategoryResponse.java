package com.gymmate.scheduling.internal.application.dto;

import com.gymmate.scheduling.internal.domain.ClassCategory;

import java.util.UUID;

public record CategoryResponse(
    UUID id,
    UUID gymId,
    String name,
    String description,
    String color,
    String icon
) {

  public static CategoryResponse from(ClassCategory c) {
    return new CategoryResponse(
        c.getId(),
        c.getGymId(),
        c.getName(),
        c.getDescription(),
        c.getColor(),
        c.getIcon());
  }
}
