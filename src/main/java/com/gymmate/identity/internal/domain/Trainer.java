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
public class Trainer extends TenantEntity {

  private UUID userId;

  // Professional info
  private 
  String[] specializations;

  private String bio;

  private BigDecimal hourlyRate;

  @Builder.Default
  private 
  BigDecimal commissionRate = BigDecimal.ZERO;

  // Certifications

  @Builder.Default
  private String certifications = "[]";

  // Availability
  private 
  String defaultAvailability;

  // Employment
  private LocalDate hireDate;

  private String employmentType; // full_time, part_time, contractor

  // Status
  @Builder.Default
  private 
  boolean acceptingClients = true;

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
