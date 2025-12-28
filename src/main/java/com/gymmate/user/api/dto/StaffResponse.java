package com.gymmate.user.api.dto;

import com.gymmate.user.domain.Staff;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record StaffResponse(
    UUID id,
    UUID userId,
    String position,
    String department,
    BigDecimal hourlyWage,
    LocalDate hireDate,
    String employmentType,
    String defaultSchedule,
    String permissions,
    boolean active
) {
    public static StaffResponse fromEntity(Staff staff) {
        return new StaffResponse(
                staff.getId(),
                staff.getUserId(),
                staff.getPosition(),
                staff.getDepartment(),
                staff.getHourlyWage(),
                staff.getHireDate(),
                staff.getEmploymentType(),
                staff.getDefaultSchedule(),
                staff.getPermissions(),
                staff.isActive()
        );
    }
}
