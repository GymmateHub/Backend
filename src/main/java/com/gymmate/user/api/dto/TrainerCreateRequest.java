package com.gymmate.user.api.dto;

import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TrainerCreateRequest(
    @NotNull(message = "User ID is required")
    UUID userId,

    String[] specializations,
    String bio,
    BigDecimal hourlyRate,
    BigDecimal commissionRate,
    LocalDate hireDate,
    String employmentType
) {}
