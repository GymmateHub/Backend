package com.gymmate.classes.api.dto;

import com.gymmate.classes.domain.GymArea;
import lombok.Data;

import java.util.UUID;

@Data
public class AreaResponse {
  private UUID id;
  private UUID gymId;
  private String name;
  private String areaType;
  private Integer capacity;

  public static AreaResponse from(GymArea a) {
    AreaResponse r = new AreaResponse();
    r.id = a.getId();
    r.gymId = a.getGymId();
    r.name = a.getName();
    r.areaType = a.getAreaType();
    r.capacity = a.getCapacity();
    return r;
  }
}

