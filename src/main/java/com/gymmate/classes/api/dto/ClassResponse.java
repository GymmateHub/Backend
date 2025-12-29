package com.gymmate.classes.api.dto;

import com.gymmate.classes.domain.GymClass;
import lombok.Data;

import java.math.BigDecimal;
import java.util.UUID;

@Data
public class ClassResponse {
  private UUID id;
  private UUID gymId;
  private UUID categoryId;
  private String name;
  private String description;
  private Integer durationMinutes;
  private Integer capacity;
  private BigDecimal price;
  private Integer creditsRequired;

  // extra fields
  private String skillLevel;
  private String ageRestriction;
  private String[] equipmentNeeded;
  private String imageUrl;
  private String videoUrl;
  private String instructions;

  public static ClassResponse from(GymClass gc) {
    ClassResponse r = new ClassResponse();
    r.id = gc.getId();
    r.gymId = gc.getGymId();
    r.categoryId = gc.getCategoryId();
    r.name = gc.getName();
    r.description = gc.getDescription();
    r.durationMinutes = gc.getDurationMinutes();
    r.capacity = gc.getCapacity();
    r.price = gc.getPrice();
    r.creditsRequired = gc.getCreditsRequired();
    r.skillLevel = gc.getSkillLevel();
    r.ageRestriction = gc.getAgeRestriction();
    r.equipmentNeeded = gc.getEquipmentNeeded();
    r.imageUrl = gc.getImageUrl();
    r.videoUrl = gc.getVideoUrl();
    r.instructions = gc.getInstructions();
    return r;
  }
}
