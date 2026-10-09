package com.gymmate.scheduling.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;

import com.gymmate.shared.constants.BookingStatus;
import com.gymmate.scheduling.internal.domain.ClassBooking;

public interface ClassBookingRepository extends DomainRepository<ClassBooking, UUID> {

  List<ClassBooking> findUpcomingByMemberId(UUID memberId, LocalDateTime fromDate);

  List<ClassBooking> findWaitlistByScheduleId(UUID scheduleId);

  long countConfirmedByScheduleId(UUID scheduleId);

  List<ClassBooking> findByGymId(UUID gymId);

  List<ClassBooking> findByMemberId(UUID memberId);

  List<ClassBooking> findByClassScheduleId(UUID classScheduleId);

  List<ClassBooking> findByGymIdAndStatus(UUID gymId, BookingStatus status);

  Optional<ClassBooking> findByClassScheduleIdAndMemberId(UUID classScheduleId, UUID memberId);

  long countByClassScheduleId(UUID classScheduleId);

  boolean existsByClassScheduleIdAndMemberId(UUID classScheduleId, UUID memberId);

  long countByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

  long countByGymIdAndStatusAndDateRange(UUID gymId, BookingStatus status, LocalDateTime startDate, LocalDateTime endDate);

  List<Object[]> countBookingsByClassForGym(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

  List<Object[]> countBookingsByDayOfWeek(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

  List<Object[]> countBookingsByTimeSlot(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);
}

