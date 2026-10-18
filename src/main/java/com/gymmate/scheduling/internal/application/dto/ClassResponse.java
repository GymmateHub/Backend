package com.gymmate.scheduling.internal.application.dto;

import com.gymmate.scheduling.internal.domain.GymClass;

import java.math.BigDecimal;
import java.util.UUID;

public record ClassResponse(
    UUID id,
    UUID categoryId,
    String name,
    String description,
    Integer durationMinutes,
    Integer capacity,
    BigDecimal price,
    Integer creditsRequired,
    // extra fields
    String skillLevel,
    String ageRestriction,
    String[] equipmentNeeded,
    String imageUrl,
    String videoUrl,
    String instructions
) {

  public static ClassResponse from(GymClass gc) {
    return new ClassResponse(
        gc.getId(),
        gc.getCategoryId(),
        gc.getName(),
        gc.getDescription(),
        gc.getDurationMinutes(),
        gc.getCapacity(),
        gc.getPrice(),
        gc.getCreditsRequired(),
        gc.getSkillLevel(),
        gc.getAgeRestriction(),
        gc.getEquipmentNeeded(),
        gc.getImageUrl(),
        gc.getVideoUrl(),
        gc.getInstructions());
  }
}
