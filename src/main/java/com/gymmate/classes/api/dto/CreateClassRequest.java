package com.gymmate.classes.api.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.util.UUID;

@Data
public class CreateClassRequest {
  private UUID gymId;
  private UUID categoryId;
  private String name;
  private String description;
  private Integer durationMinutes;
  private Integer capacity;
  private BigDecimal price;
  private Integer creditsRequired;
}

