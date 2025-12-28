package com.gymmate.user.api.dto;

import com.gymmate.user.domain.Trainer;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record TrainerResponse(
    UUID id,
    UUID userId,
    String[] specializations,
    String bio,
    BigDecimal hourlyRate,
    BigDecimal commissionRate,
    String certifications,
    String defaultAvailability,
    LocalDate hireDate,
    String employmentType,
    boolean acceptingClients,
    boolean active
) {
    public static TrainerResponse fromEntity(Trainer trainer) {
        return new TrainerResponse(
                trainer.getId(),
                trainer.getUserId(),
                trainer.getSpecializations(),
                trainer.getBio(),
                trainer.getHourlyRate(),
                trainer.getCommissionRate(),
                trainer.getCertifications(),
                trainer.getDefaultAvailability(),
                trainer.getHireDate(),
                trainer.getEmploymentType(),
                trainer.isAcceptingClients(),
                trainer.isActive()
        );
    }
}
