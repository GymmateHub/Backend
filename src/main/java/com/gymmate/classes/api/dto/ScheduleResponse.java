package com.gymmate.classes.api.dto;

import com.gymmate.classes.domain.ClassSchedule;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Data
public class ScheduleResponse {
  private UUID id;
  private UUID gymId;
  private UUID classId;
  private UUID trainerId;
  private UUID areaId;
  private LocalDateTime startTime;
  private LocalDateTime endTime;
  private Integer capacityOverride;
  private BigDecimal priceOverride;
  private String status;

  // extra fields
  private String cancellationReason;
  private String instructorNotes;
  private String adminNotes;

  public static ScheduleResponse from(ClassSchedule s) {
    ScheduleResponse r = new ScheduleResponse();
    r.id = s.getId();
    r.gymId = s.getGymId();
    r.classId = s.getClassId();
    r.trainerId = s.getTrainerId();
    r.areaId = s.getAreaId();
    r.startTime = s.getStartTime();
    r.endTime = s.getEndTime();
    r.capacityOverride = s.getCapacityOverride();
    r.priceOverride = s.getPriceOverride();
    r.status = s.getStatus() == null ? null : s.getStatus().name();
    r.cancellationReason = s.getCancellationReason();
    r.instructorNotes = s.getInstructorNotes();
    r.adminNotes = s.getAdminNotes();
    return r;
  }
}
