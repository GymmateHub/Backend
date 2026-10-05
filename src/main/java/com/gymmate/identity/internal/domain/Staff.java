package com.gymmate.identity.internal.domain;

import com.gymmate.shared.domain.TenantEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class Staff extends TenantEntity {

  private UUID userId;

  // Job details
  private String position;

  private String department; // front_desk, maintenance, management, cleaning

  private BigDecimal hourlyWage;

  // Employment
  private LocalDate hireDate;

  private String employmentType; // full_time, part_time, contractor

  // Schedule
  private 
  String defaultSchedule;

  // Permissions

  @Builder.Default
  private String permissions = "[]"; // ["access_control", "pos", "member_management"]

  public void updatePosition(String position, String department) {
    this.position = position;
    this.department = department;
  }

  public void updateWage(BigDecimal hourlyWage) {
    this.hourlyWage = hourlyWage;
  }

  public void updateSchedule(String schedule) {
    this.defaultSchedule = schedule;
  }
}
