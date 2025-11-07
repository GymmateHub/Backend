package com.gymmate.user.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "staff")
public class Staff extends BaseAuditEntity {

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  // Job details
  @Column(length = 100)
  private String position;

  @Column(length = 50)
  private String department; // front_desk, maintenance, management, cleaning

  @Column(name = "hourly_wage", precision = 10, scale = 2)
  private BigDecimal hourlyWage;

  // Employment
  @Column(name = "hire_date")
  private LocalDate hireDate;

  @Column(name = "employment_type", length = 20)
  private String employmentType; // full_time, part_time, contractor

  // Schedule
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "default_schedule", columnDefinition = "jsonb")
  private String defaultSchedule;

  // Permissions
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
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

