package com.gymmate.retail.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * MaintenanceRecord entity representing a maintenance activity on equipment.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class MaintenanceRecord extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)

  private UUID equipmentId;

  private LocalDate maintenanceDate;

  private String maintenanceType; // routine, repair, inspection, replacement

  private String description;

  private String performedBy; // Technician or staff name

  private String technicianCompany;

  private BigDecimal cost;

  private String partsReplaced;

  private LocalDate nextMaintenanceDue;

  private String notes;

  private String invoiceNumber;

  private String invoiceUrl;

  // Completion status
  @Builder.Default
  private 
  boolean completed = true;

  private String completionNotes;

  // Business methods
  public void complete(String completionNotes) {
    this.completed = true;
    this.completionNotes = completionNotes;
  }

  public void updateCost(BigDecimal cost) {
    this.cost = cost;
  }
}
