package com.gymmate.inventory.api.dto;

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
