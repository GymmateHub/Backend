package com.gymmate.inventory.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * MaintenanceRecord entity representing a maintenance activity on equipment.
 * Extends GymScopedEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "maintenance_records")
public class MaintenanceRecord extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedEntity)

  @Column(name = "equipment_id", nullable = false)
  private UUID equipmentId;

  @Column(name = "maintenance_date", nullable = false)
  private LocalDate maintenanceDate;

  @Column(name = "maintenance_type", nullable = false, length = 50)
  private String maintenanceType; // routine, repair, inspection, replacement

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(name = "performed_by", length = 200)
  private String performedBy; // Technician or staff name

  @Column(name = "technician_company", length = 200)
  private String technicianCompany;

  @Column(precision = 10, scale = 2)
  private BigDecimal cost;

  @Column(name = "parts_replaced", columnDefinition = "TEXT")
  private String partsReplaced;

  @Column(name = "next_maintenance_due")
  private LocalDate nextMaintenanceDue;

  @Column(columnDefinition = "TEXT")
  private String notes;

  @Column(name = "invoice_number", length = 100)
  private String invoiceNumber;

  @Column(name = "invoice_url", length = 500)
  private String invoiceUrl;

  // Completion status
  @Column(name = "is_completed")
  @Builder.Default
  private boolean completed = true;

  @Column(name = "completion_notes", columnDefinition = "TEXT")
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
