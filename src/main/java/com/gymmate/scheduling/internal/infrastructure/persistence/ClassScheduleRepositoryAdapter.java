package com.gymmate.scheduling.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.scheduling.internal.domain.ClassSchedule;
import com.gymmate.shared.constants.ClassScheduleStatus;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.gymmate.scheduling.internal.application.port.ClassScheduleRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link ClassScheduleRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the ClassSchedule finders live here.
 */
@Component()
@Transactional()
public class ClassScheduleRepositoryAdapter extends JpaDomainRepositoryAdapter<ClassSchedule, UUID, ClassScheduleJpaRepository>
        implements ClassScheduleRepository {

    public ClassScheduleRepositoryAdapter(ClassScheduleJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
