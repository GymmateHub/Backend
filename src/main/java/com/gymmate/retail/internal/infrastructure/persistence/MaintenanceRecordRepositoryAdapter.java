package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.retail.internal.application.port.MaintenanceRecordRepository;
import com.gymmate.retail.internal.domain.MaintenanceRecord;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link MaintenanceRecordRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the MaintenanceRecord finders live here.
 */
@Component
@Transactional()
public class MaintenanceRecordRepositoryAdapter extends JpaDomainRepositoryAdapter<MaintenanceRecord, UUID, MaintenanceRecordJpaRepository>
        implements MaintenanceRecordRepository {

    public MaintenanceRecordRepositoryAdapter(MaintenanceRecordJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
    public long countByEquipmentId(UUID equipmentId) {
        return jpaRepository.countByEquipmentId(equipmentId);
    }
}
