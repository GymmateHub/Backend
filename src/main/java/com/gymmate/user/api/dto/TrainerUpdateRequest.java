package com.gymmate.user.api.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TrainerUpdateRequest {

    private BigDecimal hourlyRate;
    private BigDecimal commissionRate;
    private String bio;
    private String[] specializations;
    private String availabilityJson;
    private String certificationsJson;
}

