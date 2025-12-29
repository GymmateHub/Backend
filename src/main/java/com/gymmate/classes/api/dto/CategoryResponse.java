package com.gymmate.classes.api.dto;

import com.gymmate.classes.domain.ClassCategory;
import lombok.Data;

import java.util.UUID;

@Data
public class CategoryResponse {
  private UUID id;
  private UUID gymId;
  private String name;
  private String description;
  private String color;

  public static CategoryResponse from(ClassCategory c) {
    CategoryResponse r = new CategoryResponse();
    r.id = c.getId();
    r.gymId = c.getGymId();
    r.name = c.getName();
    r.description = c.getDescription();
    r.color = c.getColor();
    return r;
  }
}

