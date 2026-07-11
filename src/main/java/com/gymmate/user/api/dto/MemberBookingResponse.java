package com.gymmate.user.api.dto;

import com.gymmate.classes.domain.ClassBooking;
import com.gymmate.classes.domain.ClassSchedule;
import com.gymmate.classes.domain.GymClass;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * A member's booking enriched with schedule and class details,
 * shaped for the member mobile app's bookings list.
 */
public record MemberBookingResponse(
    UUID bookingId,
    UUID scheduleId,
    UUID classId,
    String className,
    LocalDateTime startTime,
    LocalDateTime endTime,
    String status,
    LocalDateTime bookingDate,
    Integer creditsUsed,
    Integer waitlistPosition,
    String memberNotes) {

  public static MemberBookingResponse from(ClassBooking booking, ClassSchedule schedule, GymClass gymClass) {
    return new MemberBookingResponse(
        booking.getId(),
        booking.getClassScheduleId(),
        schedule != null ? schedule.getClassId() : null,
        gymClass != null ? gymClass.getName() : null,
        schedule != null ? schedule.getStartTime() : null,
        schedule != null ? schedule.getEndTime() : null,
        booking.getStatus() != null ? booking.getStatus().name() : null,
        booking.getBookingDate(),
        booking.getCreditsUsed(),
        booking.getWaitlistPosition(),
        booking.getMemberNotes());
  }
}
