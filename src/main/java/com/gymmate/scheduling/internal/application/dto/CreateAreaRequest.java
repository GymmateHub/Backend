package com.gymmate.scheduling.internal.application.dto;

import lombok.Data;

import java.util.UUID;

@Data
public class CreateAreaRequest {
  private UUID gymId;
  private String name;
  private String areaType;
  private Integer capacity;
}

