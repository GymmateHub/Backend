package com.gymmate.retail.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Equipment entity representing gym equipment and machines.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 * Equipment can be tracked at both organisation level (gymId = null)
 * or gym level (gymId set).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class Equipment extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)

  private String name;

  @Builder.Default
  private EquipmentCategory category = EquipmentCategory.OTHER;

  private String description;

  private String manufacturer;

  private String model;

  private String serialNumber;

  // Status and tracking

  @Builder.Default
  private EquipmentStatus status = EquipmentStatus.AVAILABLE;

  private LocalDate purchaseDate;

  private BigDecimal purchasePrice;

  private BigDecimal currentValue;

  // Warranty information
  private LocalDate warrantyExpiryDate;

  private String warrantyProvider;

  // Location and assignment
  private UUID areaId; // Reference to GymArea if applicable

  private String locationNotes;

  // Maintenance
  private LocalDate lastMaintenanceDate;

  private LocalDate nextMaintenanceDate;

  @Builder.Default
  private Integer maintenanceIntervalDays = 90; // Default 90 days

  @Builder.Default
  private BigDecimal totalMaintenanceCost = BigDecimal.ZERO;

  // Usage tracking
  @Builder.Default
  private Integer usageHours = 0;

  private Integer maxCapacity; // Max users at once

  // Supplier reference
  private UUID supplierId;

  // Additional info
  private String imageUrl;

  private String notes;

  // Business methods
  public void updateStatus(EquipmentStatus newStatus) {
    this.status = newStatus;
  }

  public void recordMaintenance(LocalDate maintenanceDate, BigDecimal cost) {
    this.lastMaintenanceDate = maintenanceDate;
    if (maintenanceIntervalDays != null) {
      this.nextMaintenanceDate = maintenanceDate.plusDays(maintenanceIntervalDays);
    }
    if (cost != null) {
      this.totalMaintenanceCost = (this.totalMaintenanceCost == null ? BigDecimal.ZERO : this.totalMaintenanceCost).add(cost);
    }
  }

  public void updateUsageHours(int hours) {
    this.usageHours = (this.usageHours == null ? 0 : this.usageHours) + hours;
  }

  public boolean isMaintenanceDue() {
    return nextMaintenanceDate != null && LocalDate.now().isAfter(nextMaintenanceDate);
  }

  public boolean isWarrantyValid() {
    return warrantyExpiryDate != null && LocalDate.now().isBefore(warrantyExpiryDate);
  }

  public void retire() {
    this.status = EquipmentStatus.RETIRED;
    this.setActive(false);
  }

  public void markAsAvailable() {
    this.status = EquipmentStatus.AVAILABLE;
    this.setActive(true);
  }

  public void markInUse() {
    this.status = EquipmentStatus.IN_USE;
  }

  public void markForMaintenance() {
    this.status = EquipmentStatus.MAINTENANCE;
  }
}
