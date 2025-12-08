package com.gymmate.classes.infrastructure;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

import com.gymmate.classes.domain.BookingStatus;
import com.gymmate.classes.domain.ClassBooking;

public interface ClassBookingRepository {

  void delete(ClassBooking booking);

  boolean existsByScheduleIdAndMemberId(UUID scheduleId, UUID memberId);

  List<ClassBooking> findUpcomingByMemberId(UUID memberId, LocalDateTime fromDate);

  List<ClassBooking> findWaitlistByScheduleId(UUID scheduleId);

  long countConfirmedByScheduleId(UUID scheduleId);

  long countByScheduleId(UUID scheduleId);

  Optional<ClassBooking> findByScheduleIdAndMemberId(UUID scheduleId, UUID memberId);

  List<ClassBooking> findByStatus(UUID gymId, BookingStatus status);

  List<ClassBooking> findByGymId(UUID gymId);

  List<ClassBooking> findByScheduleId(UUID scheduleId);

  List<ClassBooking> findByMemberId(UUID memberId);

  Optional<ClassBooking> findById(UUID id);

  ClassBooking save(ClassBooking booking);
}


