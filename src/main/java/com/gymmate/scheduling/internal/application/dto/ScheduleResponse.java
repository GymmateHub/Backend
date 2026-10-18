package com.gymmate.scheduling.internal.application.dto;

import com.gymmate.scheduling.internal.domain.ClassSchedule;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record ScheduleResponse(
    UUID id,
    UUID classId,
    UUID trainerId,
    UUID areaId,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Integer capacityOverride,
    BigDecimal priceOverride,
    String status,
    // extra fields
    String cancellationReason,
    String instructorNotes,
    String adminNotes
) {

  public static ScheduleResponse from(ClassSchedule s) {
    return new ScheduleResponse(
        s.getId(),
        s.getClassId(),
        s.getTrainerId(),
        s.getAreaId(),
        s.getStartTime(),
        s.getEndTime(),
        s.getCapacityOverride(),
        s.getPriceOverride(),
        s.getStatus() == null ? null : s.getStatus().name(),
        s.getCancellationReason(),
        s.getInstructorNotes(),
        s.getAdminNotes());
  }
}
