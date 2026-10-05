package com.gymmate.scheduling.internal.infrastructure.persistence;

import com.gymmate.scheduling.internal.domain.ClassSchedule;
import com.gymmate.shared.constants.ClassScheduleStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.scheduling.internal.application.port.ClassScheduleRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link ClassScheduleRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class ClassScheduleRepositoryAdapter extends DomainRepositoryAdapter implements ClassScheduleRepository {

    private final ClassScheduleJpaRepository jpaRepository;

    public ClassScheduleRepositoryAdapter(ClassScheduleJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public ClassSchedule save(ClassSchedule schedule) {
        return save(jpaRepository, schedule);
    }

    @Override
    public Optional<ClassSchedule> findById(UUID id) {
        return this.<Optional<ClassSchedule>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public List<ClassSchedule> findByGymId(UUID gymId) {
        return this.<List<ClassSchedule>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<ClassSchedule> findByClassId(UUID classId) {
        return this.<List<ClassSchedule>>fromJpa(jpaRepository.findByClassId(classId));
    }

    @Override
    public List<ClassSchedule> findByTrainerId(UUID trainerId) {
        return this.<List<ClassSchedule>>fromJpa(jpaRepository.findByTrainerId(trainerId));
    }

    @Override
    public List<ClassSchedule> findByGymIdAndDateRange(UUID gymId, LocalDateTime start, LocalDateTime end) {
        return this.<List<ClassSchedule>>fromJpa(jpaRepository.findByGymIdAndDateRange(gymId, start, end));
    }

    @Override
    public List<ClassSchedule> findAvailableSchedules(UUID gymId, LocalDateTime start, LocalDateTime end) {
        return this.<List<ClassSchedule>>fromJpa(jpaRepository.findAvailableSchedules(gymId, start, end));
    }


    @Override
    public boolean hasTrainerConflict(UUID trainerId, LocalDateTime startTime, LocalDateTime endTime) {
        return jpaRepository.hasTrainerConflict(trainerId, startTime, endTime);
    }

    @Override
    public boolean hasAreaConflict(UUID areaId, LocalDateTime startTime, LocalDateTime endTime) {
        return jpaRepository.hasAreaConflict(areaId, startTime, endTime);
    }

    @Override
    public void delete(ClassSchedule schedule) {
        delete(jpaRepository, schedule);
    }

    @Override
    public List<ClassSchedule> findByGymIdAndStatus(UUID gymId, ClassScheduleStatus status) {
        return this.<List<ClassSchedule>>fromJpa(jpaRepository.findByGymIdAndStatus(gymId, status));
    }

    @Override
    public long countByGymIdAndStartTimeBetween(UUID gymId, LocalDateTime startTime, LocalDateTime endTime) {
        return jpaRepository.countByGymIdAndStartTimeBetween(gymId, startTime, endTime);
    }

    @Override
    public int incrementBookedCountIfCapacityAvailable(UUID id, int capacity) {
        return jpaRepository.incrementBookedCountIfCapacityAvailable(id, capacity);
    }

    @Override
    public int decrementBookedCount(UUID id) {
        return jpaRepository.decrementBookedCount(id);
    }

    @Override
    public List<ClassSchedule> saveAll(Iterable<ClassSchedule> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<ClassSchedule> findAll() {
        return this.<List<ClassSchedule>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<ClassSchedule> findAllById(Iterable<UUID> ids) {
        return this.<List<ClassSchedule>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<ClassSchedule> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public ClassSchedule saveAndFlush(ClassSchedule entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<ClassSchedule> findAll(Pageable pageable) {
        return this.<Page<ClassSchedule>>fromJpa(jpaRepository.findAll(pageable));
    }
}
