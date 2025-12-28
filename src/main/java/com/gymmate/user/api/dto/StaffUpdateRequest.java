package com.gymmate.user.api.dto;

import java.math.BigDecimal;

public record StaffUpdateRequest(
    String position,
    String department,
    BigDecimal hourlyWage,
    String scheduleJson,
    String permissionsJson
) {}
