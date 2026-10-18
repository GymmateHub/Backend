package com.gymmate.retail.internal.application.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * DTO for recording maintenance on equipment.
 */
public record MaintenanceRecordRequest(
  LocalDate maintenanceDate,
  BigDecimal cost
) {
}
