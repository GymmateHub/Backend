package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.retail.internal.application.port.MaintenanceRecordRepository;
import com.gymmate.retail.internal.domain.MaintenanceRecord;
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
 * Persistence adapter implementing {@link MaintenanceRecordRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class MaintenanceRecordRepositoryAdapter extends DomainRepositoryAdapter implements MaintenanceRecordRepository {

    private final MaintenanceRecordJpaRepository jpaRepository;

    public MaintenanceRecordRepositoryAdapter(MaintenanceRecordJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public MaintenanceRecord save(MaintenanceRecord maintenanceRecord) {
        return save(jpaRepository, maintenanceRecord);
    }

    @Override
    public Optional<MaintenanceRecord> findById(UUID id) {
        return this.<Optional<MaintenanceRecord>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public List<MaintenanceRecord> findByEquipmentId(UUID equipmentId) {
        return this.<List<MaintenanceRecord>>fromJpa(jpaRepository.findByEquipmentId(equipmentId));
    }

    @Override
    public List<MaintenanceRecord> findByGymId(UUID gymId) {
        return this.<List<MaintenanceRecord>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<MaintenanceRecord> findByOrganisationId(UUID organisationId) {
        return this.<List<MaintenanceRecord>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public List<MaintenanceRecord> findByEquipmentIdOrderByMaintenanceDateDesc(UUID equipmentId) {
        return this.<List<MaintenanceRecord>>fromJpa(jpaRepository.findByEquipmentIdOrderByMaintenanceDateDesc(equipmentId));
    }

    @Override
    public List<MaintenanceRecord> findByGymIdAndDateRange(UUID gymId, LocalDate startDate, LocalDate endDate) {
        return this.<List<MaintenanceRecord>>fromJpa(jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate));
    }

    @Override
    public List<MaintenanceRecord> findByOrganisationIdAndDateRange(UUID organisationId, LocalDate startDate, LocalDate endDate) {
        return this.<List<MaintenanceRecord>>fromJpa(jpaRepository.findByOrganisationIdAndDateRange(organisationId, startDate, endDate));
    }

    @Override
    public List<MaintenanceRecord> findIncompleteByGymId(UUID gymId) {
        return this.<List<MaintenanceRecord>>fromJpa(jpaRepository.findIncompleteByGymId(gymId));
    }

    @Override
    public void delete(MaintenanceRecord maintenanceRecord) {
        delete(jpaRepository, maintenanceRecord);
    }

    @Override
    public long countByEquipmentId(UUID equipmentId) {
        return jpaRepository.countByEquipmentId(equipmentId);
    }

    @Override
    public List<MaintenanceRecord> saveAll(Iterable<MaintenanceRecord> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<MaintenanceRecord> findAll() {
        return this.<List<MaintenanceRecord>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<MaintenanceRecord> findAllById(Iterable<UUID> ids) {
        return this.<List<MaintenanceRecord>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<MaintenanceRecord> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public MaintenanceRecord saveAndFlush(MaintenanceRecord entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<MaintenanceRecord> findAll(Pageable pageable) {
        return this.<Page<MaintenanceRecord>>fromJpa(jpaRepository.findAll(pageable));
    }
}
