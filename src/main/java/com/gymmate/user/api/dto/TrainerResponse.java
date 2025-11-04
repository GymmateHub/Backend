package com.gymmate.user.api.dto;

import com.gymmate.user.domain.Trainer;
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
public class TrainerResponse {

    private UUID id;
    private UUID userId;
    private String[] specializations;
    private String bio;
    private BigDecimal hourlyRate;
    private BigDecimal commissionRate;
    private String certifications;
    private String defaultAvailability;
    private LocalDate hireDate;
    private String employmentType;
    private boolean acceptingClients;
    private boolean active;

    public static TrainerResponse fromEntity(Trainer trainer) {
        return TrainerResponse.builder()
                .id(trainer.getId())
                .userId(trainer.getUserId())
                .specializations(trainer.getSpecializations())
                .bio(trainer.getBio())
                .hourlyRate(trainer.getHourlyRate())
                .commissionRate(trainer.getCommissionRate())
                .certifications(trainer.getCertifications())
                .defaultAvailability(trainer.getDefaultAvailability())
                .hireDate(trainer.getHireDate())
                .employmentType(trainer.getEmploymentType())
                .acceptingClients(trainer.isAcceptingClients())
                .active(trainer.isActive())
                .build();
    }
}

