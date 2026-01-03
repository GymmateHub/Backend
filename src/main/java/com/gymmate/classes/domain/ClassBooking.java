package com.gymmate.classes.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ClassBooking entity representing a member's booking for a class.
 * Extends GymScopedEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "class_bookings")
public class ClassBooking extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedEntity)
  @Column(name = "member_id", nullable = false)
  private UUID memberId;

  @Column(name = "class_schedule_id", nullable = false)
  private UUID classScheduleId;

  @Column(name = "booking_date")
  @Builder.Default
  private LocalDateTime bookingDate = LocalDateTime.now();

  @Enumerated(EnumType.STRING)
  @Column(length = 20)
  @Builder.Default
  private BookingStatus status = BookingStatus.CONFIRMED;

  // Payment
  @Column(name = "credits_used")
  @Builder.Default
  private Integer creditsUsed = 1;

  @Column(name = "amount_paid", precision = 10, scale = 2)
  @Builder.Default
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

  public void checkIn() {
    this.checkedInAt = LocalDateTime.now();
    this.status = BookingStatus.CONFIRMED;
  }

  public void checkOut() {
    this.checkedOutAt = LocalDateTime.now();
    this.status = BookingStatus.COMPLETED;
  }

  public void cancel(String reason) {
    this.status = BookingStatus.CANCELLED;
    this.cancelledAt = LocalDateTime.now();
    this.cancellationReason = reason;
  }

  public void markNoShow() {
    this.status = BookingStatus.NO_SHOW;
  }

  public void waitlist() {
    this.status = BookingStatus.WAITLISTED;
  }

  public boolean isConfirmed() {
    return status == BookingStatus.CONFIRMED;
  }

  public boolean isCheckedIn() {
    return checkedInAt != null;
  }
}

