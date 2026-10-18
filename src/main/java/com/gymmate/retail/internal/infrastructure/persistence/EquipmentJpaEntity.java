package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import com.gymmate.retail.internal.domain.EquipmentStatus;
import com.gymmate.retail.internal.domain.EquipmentCategory;
import com.gymmate.retail.internal.domain.Equipment;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link Equipment} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "Equipment")
@Table(name = "equipment")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(Equipment.class)
public class EquipmentJpaEntity extends GymScopedJpaEntity {

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
    @Column(nullable = false, length = 200)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
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
    private Integer maintenanceIntervalDays = 90; // Default 90 days

    @Column(name = "total_maintenance_cost", precision = 10, scale = 2)
    private BigDecimal totalMaintenanceCost = BigDecimal.ZERO;

    // Usage tracking
    @Column(name = "usage_hours")
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
}
