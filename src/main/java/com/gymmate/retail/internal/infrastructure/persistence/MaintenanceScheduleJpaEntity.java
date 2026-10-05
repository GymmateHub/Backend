package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;
import java.util.UUID;
import com.gymmate.retail.internal.domain.MaintenanceSchedule;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link MaintenanceSchedule} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "MaintenanceSchedule")
@Table(name = "maintenance_schedules")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(MaintenanceSchedule.class)
public class MaintenanceScheduleJpaEntity extends GymScopedJpaEntity {

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
    @Column(name = "equipment_id", nullable = false)
    private UUID equipmentId;

    @Column(name = "schedule_name", nullable = false, length = 200)
    private String scheduleName;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(name = "scheduled_date", nullable = false)
    private LocalDate scheduledDate;

    @Column(name = "maintenance_type", nullable = false, length = 50)
    private String // routine, inspection, deep_clean, calibration
    maintenanceType;

    @Column(name = "assigned_to", length = 200)
    private String // Staff member or company
    assignedTo;

    @Column(name = "estimated_duration_hours")
    private Integer estimatedDurationHours;

    @Column(name = "is_recurring")
    private boolean recurring = false;

    @Column(name = "recurrence_interval_days")
    private Integer // For recurring schedules
    recurrenceIntervalDays;

    @Column(name = "is_completed")
    private boolean completed = false;

    @Column(name = "completed_date")
    private LocalDate completedDate;

    @Column(name = "maintenance_record_id")
    private UUID // Link to actual maintenance record once completed
    maintenanceRecordId;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "reminder_sent")
    private boolean reminderSent = false;

    @Column(name = "reminder_date")
    private LocalDate reminderDate;
}
