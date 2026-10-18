package com.gymmate.scheduling.internal.infrastructure.persistence;

import com.gymmate.shared.constants.BookingStatus;
import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.scheduling.internal.domain.ClassBooking;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link ClassBooking} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "ClassBooking")
@Table(name = "class_bookings")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(ClassBooking.class)
public class ClassBookingJpaEntity extends GymScopedJpaEntity {

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
    @Column(name = "member_id", nullable = false)
    private UUID memberId;

    @Column(name = "class_schedule_id", nullable = false)
    private UUID classScheduleId;

    @Column(name = "booking_date")
    private LocalDateTime bookingDate = LocalDateTime.now();

    @Enumerated(EnumType.STRING)
    @Column(length = 20)
    private BookingStatus status = BookingStatus.CONFIRMED;

    // Payment
    @Column(name = "credits_used")
    private Integer creditsUsed = 1;

    @Column(name = "amount_paid", precision = 10, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    // Attendance
    @Column(name = "checked_in_at")
    private LocalDateTime checkedInAt;

    @Column(name = "checked_out_at")
    private LocalDateTime checkedOutAt;

    // Cancellation
    @Column(name = "cancelled_at")
    private LocalDateTime cancelledAt;

    @Column(name = "cancellation_reason", columnDefinition = "TEXT")
    private String cancellationReason;

    // Notes
    @Column(name = "member_notes", columnDefinition = "TEXT")
    private String memberNotes;

    // Waitlist tracking
    @Column(name = "waitlist_position")
    private Integer waitlistPosition;
}
