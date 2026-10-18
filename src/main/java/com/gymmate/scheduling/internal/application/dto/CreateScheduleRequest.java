package com.gymmate.scheduling.internal.application.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record CreateScheduleRequest(
    UUID gymId,
    UUID classId,
    UUID trainerId,
    UUID areaId,
    LocalDateTime startTime,
    LocalDateTime endTime,
    Integer capacityOverride,
    BigDecimal priceOverride
) {
}
