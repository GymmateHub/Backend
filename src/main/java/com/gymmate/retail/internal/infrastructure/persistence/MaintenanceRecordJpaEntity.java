package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;
import com.gymmate.retail.internal.domain.MaintenanceRecord;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link MaintenanceRecord} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "MaintenanceRecord")
@Table(name = "maintenance_records")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(MaintenanceRecord.class)
public class MaintenanceRecordJpaEntity extends GymScopedJpaEntity {

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
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
    private boolean completed = true;

    @Column(name = "completion_notes", columnDefinition = "TEXT")
    private String completionNotes;
}
