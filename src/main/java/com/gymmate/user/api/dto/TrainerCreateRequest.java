package com.gymmate.user.api.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerCreateRequest {

    @NotNull(message = "User ID is required")
    private UUID userId;

    private String[] specializations;
    private String bio;
    private BigDecimal hourlyRate;
    private BigDecimal commissionRate;
    private LocalDate hireDate;
    private String employmentType;
}

