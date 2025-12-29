package com.gymmate.classes.api.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateCategoryRequest {
  private UUID gymId;
  private String name;
  private String description;
  private String color;
}

