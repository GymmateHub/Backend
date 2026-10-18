package com.gymmate.scheduling.internal.domain;

import com.gymmate.shared.constants.BookingStatus;
import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * ClassBooking entity representing a member's booking for a class.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class ClassBooking extends GymScopedEntity {

  // Note: gymId is inherited from GymScopedJpaEntity
  // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
  private UUID memberId;

  private UUID classScheduleId;

  @Builder.Default
  private LocalDateTime bookingDate = LocalDateTime.now();

  @Builder.Default
  private BookingStatus status = BookingStatus.CONFIRMED;

  // Payment
  @Builder.Default
  private Integer creditsUsed = 1;

  @Builder.Default
  private BigDecimal amountPaid = BigDecimal.ZERO;

  // Attendance
  private LocalDateTime checkedInAt;

  private LocalDateTime checkedOutAt;

  // Cancellation
  private LocalDateTime cancelledAt;

  private String cancellationReason;

  // Notes
  private String memberNotes;

  // Waitlist tracking
  private Integer waitlistPosition;

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
