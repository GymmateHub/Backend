package com.gymmate.scheduling.internal.infrastructure.persistence;

import com.gymmate.shared.constants.ClassScheduleStatus;
import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.scheduling.internal.domain.ClassSchedule;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link ClassSchedule} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "ClassSchedule")
@Table(name = "class_schedules")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(ClassSchedule.class)
public class ClassScheduleJpaEntity extends GymScopedJpaEntity {

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
    @Column(name = "class_id", nullable = false)
    private UUID classId;

    @Column(name = "trainer_id")
    private UUID trainerId;

    @Column(name = "area_id")
    private UUID areaId;

    // Timing
    @Column(name = "start_time", nullable = false)
    private LocalDateTime startTime;

    @Column(name = "end_time", nullable = false)
    private LocalDateTime endTime;

    // Overrides for this specific instance
    @Column(name = "capacity_override")
    private Integer capacityOverride;

    // Confirmed booking count, kept in sync by an atomic conditional UPDATE in
    // ClassBookingService (see enforce_class_schedule_capacity trigger, V13 migration).
    // Do not set directly outside that path.
    @Column(name = "booked_count", nullable = false)
    private Integer bookedCount = 0;

    @Column(name = "price_override", precision = 10, scale = 2)
    private BigDecimal priceOverride;

    // Status
    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private ClassScheduleStatus status = ClassScheduleStatus.SCHEDULED;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // Notes
    @Column(name = "instructor_notes", columnDefinition = "TEXT")
    private String instructorNotes;

    @Column(name = "admin_notes", columnDefinition = "TEXT")
    private String adminNotes;
}
