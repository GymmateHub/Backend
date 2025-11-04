package com.gymmate.user.domain;

import com.gymmate.shared.domain.BaseAuditEntity;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "trainers")
public class Trainer extends BaseAuditEntity {

  @Column(name = "user_id", nullable = false)
  private UUID userId;

  // Professional info
  @JdbcTypeCode(SqlTypes.ARRAY)
  @Column(columnDefinition = "text[]")
  private String[] specializations;

  @Column(columnDefinition = "TEXT")
  private String bio;

  @Column(name = "hourly_rate", precision = 10, scale = 2)
  private BigDecimal hourlyRate;

  @Column(name = "commission_rate", precision = 5, scale = 2)
  @Builder.Default
  private BigDecimal commissionRate = BigDecimal.ZERO;

  // Certifications
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(columnDefinition = "jsonb")
  @Builder.Default
  private String certifications = "[]";

  // Availability
  @JdbcTypeCode(SqlTypes.JSON)
  @Column(name = "default_availability", columnDefinition = "jsonb")
  private String defaultAvailability;

  // Employment
  @Column(name = "hire_date")
  private LocalDate hireDate;

  @Column(name = "employment_type", length = 20)
  private String employmentType; // full_time, part_time, contractor

  // Status
  @Column(name = "is_accepting_clients")
  @Builder.Default
  private boolean acceptingClients = true;

  public void updateRate(BigDecimal hourlyRate, BigDecimal commissionRate) {
    this.hourlyRate = hourlyRate;
    this.commissionRate = commissionRate;
  }

  public void updateAvailability(String availability) {
    this.defaultAvailability = availability;
  }

  public void toggleAcceptingClients() {
    this.acceptingClients = !this.acceptingClients;
  }
}

