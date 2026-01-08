package com.gymmate.inventory.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

/**
 * Equipment entity representing gym equipment and machines.
 * Extends GymScopedEntity for automatic organisation and gym filtering.
 * Equipment can be tracked at both organisation level (gymId = null) 
 * or gym level (gymId set).
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "equipment")
public class Equipment extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedEntity)

  @Column(nullable = false, length = 200)
  private String name;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 50)
  @Builder.Default
  private EquipmentCategory category = EquipmentCategory.OTHER;

  @Column(columnDefinition = "TEXT")
  private String description;

  @Column(length = 100)
  private String manufacturer;

  @Column(length = 100)
  private String model;

  @Column(name = "serial_number", length = 100)
  private String serialNumber;

  // Status and tracking
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 20)
  @Builder.Default
  private EquipmentStatus status = EquipmentStatus.AVAILABLE;

  @Column(name = "purchase_date")
  private LocalDate purchaseDate;

  @Column(name = "purchase_price", precision = 10, scale = 2)
  private BigDecimal purchasePrice;

  @Column(name = "current_value", precision = 10, scale = 2)
  private BigDecimal currentValue;

  // Warranty information
  @Column(name = "warranty_expiry_date")
  private LocalDate warrantyExpiryDate;

  @Column(name = "warranty_provider", length = 200)
  private String warrantyProvider;

  // Location and assignment
  @Column(name = "area_id")
  private UUID areaId; // Reference to GymArea if applicable

  @Column(name = "location_notes", columnDefinition = "TEXT")
  private String locationNotes;

  // Maintenance
  @Column(name = "last_maintenance_date")
  private LocalDate lastMaintenanceDate;

  @Column(name = "next_maintenance_date")
  private LocalDate nextMaintenanceDate;

  @Column(name = "maintenance_interval_days")
  @Builder.Default
  private Integer maintenanceIntervalDays = 90; // Default 90 days

  @Column(name = "total_maintenance_cost", precision = 10, scale = 2)
  @Builder.Default
  private BigDecimal totalMaintenanceCost = BigDecimal.ZERO;

  // Usage tracking
  @Column(name = "usage_hours")
  @Builder.Default
  private Integer usageHours = 0;

  @Column(name = "max_capacity")
  private Integer maxCapacity; // Max users at once

  // Supplier reference
  @Column(name = "supplier_id")
  private UUID supplierId;

  // Additional info
  @Column(name = "image_url", length = 500)
  private String imageUrl;

  @Column(columnDefinition = "TEXT")
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
