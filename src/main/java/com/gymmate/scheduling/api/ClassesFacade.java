package com.gymmate.scheduling.api;

import com.gymmate.shared.constants.BookingStatus;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Public read surface for class/schedule/booking aggregate counts, used by
 * {@code analytics} for reporting. Signatures mirror the underlying JPA
 * repository queries verbatim — this is a relocation of ownership, not a
 * reshape (the two {@code List<Object[]>} aggregate returns are not typed
 * DTOs yet).
 */
public interface ClassesFacade {

    long countByGymId(UUID gymId);

    long countByGymIdAndStartTimeBetween(UUID gymId, LocalDateTime startTime, LocalDateTime endTime);

    long countByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

    long countByGymIdAndStatusAndDateRange(UUID gymId, BookingStatus status, LocalDateTime startDate,
            LocalDateTime endDate);

    List<Object[]> countBookingsByClassForGym(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

    List<Object[]> countBookingsByDayOfWeek(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

    List<Object[]> countBookingsByTimeSlot(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);
}
