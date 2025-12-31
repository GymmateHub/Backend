package com.gymmate.classes.api.dto;

import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class CreateScheduleRequest {
  private UUID gymId;
  private UUID classId;
  private UUID trainerId;
  private UUID areaId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Integer capacityOverride;
  private BigDecimal priceOverride;
}

