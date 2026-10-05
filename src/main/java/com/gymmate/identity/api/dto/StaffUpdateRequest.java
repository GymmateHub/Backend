package com.gymmate.identity.api.dto;

import java.math.BigDecimal;

public record StaffUpdateRequest(
    String position,
    String department,
    BigDecimal hourlyWage,
    String scheduleJson,
    String permissionsJson
) {}
