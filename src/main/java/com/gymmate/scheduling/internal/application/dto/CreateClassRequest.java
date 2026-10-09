package com.gymmate.scheduling.internal.application.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import java.math.BigDecimal;
import java.util.UUID;

public record CreateClassRequest(
    UUID gymId,
    String categoryId,
    String name,
    String description,
    @JsonAlias({"defaultDuration", "duration"})
    Integer durationMinutes,
    @JsonAlias({"defaultCapacity"})
    Integer capacity,
    BigDecimal price,
    Integer creditsRequired
) {

  public UUID getParsedCategoryId() {
    if (categoryId == null || categoryId.isBlank() || "general".equalsIgnoreCase(categoryId) || "all".equalsIgnoreCase(categoryId)) {
      return null;
    }
    try {
      return UUID.fromString(categoryId);
    } catch (IllegalArgumentException e) {
      return null;
    }
  }
}
