package com.gymmate.scheduling.internal.application.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateCategoryRequest {
  private UUID gymId;
  private String name;
  private String description;
  private String color;
}

