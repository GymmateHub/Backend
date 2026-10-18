package com.gymmate.scheduling.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;
import com.gymmate.shared.constants.BookingStatus;
import com.gymmate.scheduling.internal.domain.ClassBooking;
import com.gymmate.scheduling.internal.application.port.ClassBookingRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link ClassBookingRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the ClassBooking finders live here.
 */
@Component()
@Transactional()
public class ClassBookingRepositoryAdapter extends JpaDomainRepositoryAdapter<ClassBooking, UUID, ClassBookingJpaRepository>
        implements ClassBookingRepository {

    public ClassBookingRepositoryAdapter(ClassBookingJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<ClassBooking> findUpcomingByMemberId(UUID memberId, LocalDateTime fromDate) {
        return this.<List<ClassBooking>>fromJpa(jpaRepository.findUpcomingByMemberId(memberId, fromDate));
    }

    @Override
    public List<ClassBooking> findWaitlistByScheduleId(UUID scheduleId) {
        return this.<List<ClassBooking>>fromJpa(jpaRepository.findWaitlistByScheduleId(scheduleId));
    }

    @Override
    public long countConfirmedByScheduleId(UUID scheduleId) {
        return jpaRepository.countConfirmedByScheduleId(scheduleId);
    }

    @Override
    public List<ClassBooking> findByGymId(UUID gymId) {
        return this.<List<ClassBooking>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<ClassBooking> findByMemberId(UUID memberId) {
        return this.<List<ClassBooking>>fromJpa(jpaRepository.findByMemberId(memberId));
    }

    @Override
    public List<ClassBooking> findByClassScheduleId(UUID classScheduleId) {
        return this.<List<ClassBooking>>fromJpa(jpaRepository.findByClassScheduleId(classScheduleId));
    }

    @Override
    public List<ClassBooking> findByGymIdAndStatus(UUID gymId, BookingStatus status) {
        return this.<List<ClassBooking>>fromJpa(jpaRepository.findByGymIdAndStatus(gymId, status));
    }

    @Override
    public Optional<ClassBooking> findByClassScheduleIdAndMemberId(UUID classScheduleId, UUID memberId) {
        return this.<Optional<ClassBooking>>fromJpa(jpaRepository.findByClassScheduleIdAndMemberId(classScheduleId, memberId));
    }

    @Override
    public long countByClassScheduleId(UUID classScheduleId) {
        return jpaRepository.countByClassScheduleId(classScheduleId);
    }

    @Override
    public boolean existsByClassScheduleIdAndMemberId(UUID classScheduleId, UUID memberId) {
        return jpaRepository.existsByClassScheduleIdAndMemberId(classScheduleId, memberId);
    }

    @Override
    public long countByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.countByGymIdAndDateRange(gymId, startDate, endDate);
    }

    @Override
    public long countByGymIdAndStatusAndDateRange(UUID gymId, BookingStatus status, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.countByGymIdAndStatusAndDateRange(gymId, status, startDate, endDate);
    }

    @Override
    public List<Object[]> countBookingsByClassForGym(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.countBookingsByClassForGym(gymId, startDate, endDate);
    }

    @Override
    public List<Object[]> countBookingsByDayOfWeek(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.countBookingsByDayOfWeek(gymId, startDate, endDate);
    }

    @Override
    public List<Object[]> countBookingsByTimeSlot(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.countBookingsByTimeSlot(gymId, startDate, endDate);
    }
}
