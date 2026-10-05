package com.gymmate.scheduling.internal.infrastructure.persistence;

import java.util.UUID;
import java.util.Optional;
import java.util.List;
import java.time.LocalDateTime;
import com.gymmate.shared.constants.BookingStatus;
import com.gymmate.scheduling.internal.domain.ClassBooking;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.scheduling.internal.application.port.ClassBookingRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link ClassBookingRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class ClassBookingRepositoryAdapter extends DomainRepositoryAdapter implements ClassBookingRepository {

    private final ClassBookingJpaRepository jpaRepository;

    public ClassBookingRepositoryAdapter(ClassBookingJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public void delete(ClassBooking booking) {
        delete(jpaRepository, booking);
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
    public Optional<ClassBooking> findById(UUID id) {
        return this.<Optional<ClassBooking>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public ClassBooking save(ClassBooking booking) {
        return save(jpaRepository, booking);
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

    @Override
    public List<ClassBooking> saveAll(Iterable<ClassBooking> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<ClassBooking> findAll() {
        return this.<List<ClassBooking>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<ClassBooking> findAllById(Iterable<UUID> ids) {
        return this.<List<ClassBooking>>fromJpa(jpaRepository.findAllById(ids));
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteAll(Iterable<ClassBooking> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public ClassBooking saveAndFlush(ClassBooking entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<ClassBooking> findAll(Pageable pageable) {
        return this.<Page<ClassBooking>>fromJpa(jpaRepository.findAll(pageable));
    }
}
