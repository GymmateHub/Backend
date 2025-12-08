package com.gymmate.classes.domain;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository interface for ClassBooking domain entity.
 */
public interface ClassBookingRepository {

  ClassBooking save(ClassBooking booking);

  Optional<ClassBooking> findById(UUID id);

  List<ClassBooking> findByMemberId(UUID memberId);

  List<ClassBooking> findByScheduleId(UUID scheduleId);

  List<ClassBooking> findByGymId(UUID gymId);

  List<ClassBooking> findByStatus(UUID gymId, BookingStatus status);

  Optional<ClassBooking> findByScheduleIdAndMemberId(UUID scheduleId, UUID memberId);

  long countByScheduleId(UUID scheduleId);

  long countConfirmedByScheduleId(UUID scheduleId);

  List<ClassBooking> findWaitlistByScheduleId(UUID scheduleId);

  List<ClassBooking> findUpcomingByMemberId(UUID memberId, LocalDateTime fromDate);

  boolean existsByScheduleIdAndMemberId(UUID scheduleId, UUID memberId);

  void delete(ClassBooking booking);
}

