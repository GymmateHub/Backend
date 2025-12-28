package com.gymmate.classes.api.dto;

import com.gymmate.classes.domain.ClassBooking;
import java.time.LocalDateTime;
import java.util.UUID;

public record BookingResponse(
  UUID id,
  UUID memberId,
  UUID classScheduleId,
  LocalDateTime bookingDate,
  String status,
  Integer creditsUsed,
  String memberNotes
) {
  public static BookingResponse from(ClassBooking b) {
    return new BookingResponse(b.getId(), b.getMemberId(), b.getClassScheduleId(), b.getBookingDate(), b.getStatus().name(), b.getCreditsUsed(), b.getMemberNotes());
  }
}

