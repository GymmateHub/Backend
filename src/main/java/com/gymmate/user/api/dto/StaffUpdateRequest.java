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
public class StaffUpdateRequest {

    private String position;
    private String department;
    private BigDecimal hourlyWage;
    private String scheduleJson;
    private String permissionsJson;
}
