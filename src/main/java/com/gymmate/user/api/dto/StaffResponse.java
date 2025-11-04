package com.gymmate.user.api.dto;

import com.gymmate.user.domain.Staff;
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
public class StaffResponse {

    private UUID id;
    private UUID userId;
    private String position;
    private String department;
    private BigDecimal hourlyWage;
    private LocalDate hireDate;
    private String employmentType;
    private String defaultSchedule;
    private String permissions;
    private boolean active;

    public static StaffResponse fromEntity(Staff staff) {
        return StaffResponse.builder()
                .id(staff.getId())
                .userId(staff.getUserId())
                .position(staff.getPosition())
                .department(staff.getDepartment())
                .hourlyWage(staff.getHourlyWage())
                .hireDate(staff.getHireDate())
                .employmentType(staff.getEmploymentType())
                .defaultSchedule(staff.getDefaultSchedule())
                .permissions(staff.getPermissions())
                .active(staff.isActive())
                .build();
    }
}

