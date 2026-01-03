package com.gymmate.classes.api.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateAreaRequest {
  private UUID gymId;
  private String name;
  private String areaType;
  private Integer capacity;
}

