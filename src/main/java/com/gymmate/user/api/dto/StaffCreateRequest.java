package com.gymmate.user.api.dto;

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/** Staff create request DTO. */
public record StaffCreateRequest(
    @NotNull(message = "User ID is required") UUID userId,
    String position,
    String department,
    BigDecimal hourlyWage,
    LocalDate hireDate,
    String employmentType
) {}
