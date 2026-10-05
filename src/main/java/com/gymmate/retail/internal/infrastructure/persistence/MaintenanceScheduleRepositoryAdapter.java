package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.retail.internal.application.port.MaintenanceScheduleRepository;
import com.gymmate.retail.internal.domain.MaintenanceSchedule;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link MaintenanceScheduleRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class MaintenanceScheduleRepositoryAdapter extends DomainRepositoryAdapter implements MaintenanceScheduleRepository {

    private final MaintenanceScheduleJpaRepository jpaRepository;

    public MaintenanceScheduleRepositoryAdapter(MaintenanceScheduleJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MaintenanceSchedule save(MaintenanceSchedule maintenanceSchedule) {
        return save(jpaRepository, maintenanceSchedule);
    }

    @Override
    public Optional<MaintenanceSchedule> findById(UUID id) {
        return this.<Optional<MaintenanceSchedule>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public List<MaintenanceSchedule> findByEquipmentId(UUID equipmentId) {
        return this.<List<MaintenanceSchedule>>fromJpa(jpaRepository.findByEquipmentId(equipmentId));
    }

    @Override
    public List<MaintenanceSchedule> findByGymId(UUID gymId) {
        return this.<List<MaintenanceSchedule>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<MaintenanceSchedule> findByOrganisationId(UUID organisationId) {
        return this.<List<MaintenanceSchedule>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public List<MaintenanceSchedule> findPendingByGymId(UUID gymId) {
        return this.<List<MaintenanceSchedule>>fromJpa(jpaRepository.findPendingByGymId(gymId));
    }

    @Override
    public List<MaintenanceSchedule> findPendingByOrganisationId(UUID organisationId) {
        return this.<List<MaintenanceSchedule>>fromJpa(jpaRepository.findPendingByOrganisationId(organisationId));
    }

    @Override
    public List<MaintenanceSchedule> findDueByGymId(UUID gymId, LocalDate date) {
        return this.<List<MaintenanceSchedule>>fromJpa(jpaRepository.findDueByGymId(gymId, date));
    }

    @Override
    public List<MaintenanceSchedule> findDueByOrganisationId(UUID organisationId, LocalDate date) {
        return this.<List<MaintenanceSchedule>>fromJpa(jpaRepository.findDueByOrganisationId(organisationId, date));
    }

    @Override
    public List<MaintenanceSchedule> findByGymIdAndDateRange(UUID gymId, LocalDate startDate, LocalDate endDate) {
        return this.<List<MaintenanceSchedule>>fromJpa(jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate));
    }

    @Override
    public void delete(MaintenanceSchedule maintenanceSchedule) {
        delete(jpaRepository, maintenanceSchedule);
    }

    @Override
    public long countByEquipmentId(UUID equipmentId) {
        return jpaRepository.countByEquipmentId(equipmentId);
    }

    @Override
    public List<MaintenanceSchedule> saveAll(Iterable<MaintenanceSchedule> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<MaintenanceSchedule> findAll() {
        return this.<List<MaintenanceSchedule>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<MaintenanceSchedule> findAllById(Iterable<UUID> ids) {
        return this.<List<MaintenanceSchedule>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<MaintenanceSchedule> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public MaintenanceSchedule saveAndFlush(MaintenanceSchedule entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<MaintenanceSchedule> findAll(Pageable pageable) {
        return this.<Page<MaintenanceSchedule>>fromJpa(jpaRepository.findAll(pageable));
    }
}
