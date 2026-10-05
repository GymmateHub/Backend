package com.gymmate.scheduling.internal.application;

import com.gymmate.scheduling.api.ClassesFacade;
import com.gymmate.scheduling.internal.application.port.ClassBookingRepository;
import com.gymmate.scheduling.internal.application.port.ClassScheduleRepository;
import com.gymmate.scheduling.internal.application.port.GymClassRepository;
import com.gymmate.shared.constants.BookingStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ClassesFacadeImpl implements ClassesFacade {

    private final GymClassRepository gymClassRepository;
    private final ClassScheduleRepository classScheduleRepository;
    private final ClassBookingRepository classBookingRepository;

    @Override
    public long countByGymId(UUID gymId) {
        return gymClassRepository.countByGymId(gymId);
    }

    @Override
    public long countByGymIdAndStartTimeBetween(UUID gymId, LocalDateTime startTime, LocalDateTime endTime) {
        return classScheduleRepository.countByGymIdAndStartTimeBetween(gymId, startTime, endTime);
    }

    @Override
    public long countByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return classBookingRepository.countByGymIdAndDateRange(gymId, startDate, endDate);
    }

    @Override
    public long countByGymIdAndStatusAndDateRange(UUID gymId, BookingStatus status, LocalDateTime startDate,
            LocalDateTime endDate) {
        return classBookingRepository.countByGymIdAndStatusAndDateRange(gymId, status, startDate, endDate);
    }

    @Override
    public List<Object[]> countBookingsByClassForGym(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return classBookingRepository.countBookingsByClassForGym(gymId, startDate, endDate);
    }

    @Override
    public List<Object[]> countBookingsByDayOfWeek(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return classBookingRepository.countBookingsByDayOfWeek(gymId, startDate, endDate);
    }

    @Override
    public List<Object[]> countBookingsByTimeSlot(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return classBookingRepository.countBookingsByTimeSlot(gymId, startDate, endDate);
    }
}
