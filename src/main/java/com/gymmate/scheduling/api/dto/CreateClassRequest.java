package com.gymmate.scheduling.api.dto;

import com.fasterxml.jackson.annotation.JsonAlias;
import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateClassRequest {
  private UUID gymId;
  private String categoryId;
  private String name;
  private String description;

  @JsonAlias({"defaultDuration", "duration"})
  private Integer durationMinutes;

  @JsonAlias({"defaultCapacity"})
  private Integer capacity;

  private BigDecimal price;
  private Integer creditsRequired;

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

