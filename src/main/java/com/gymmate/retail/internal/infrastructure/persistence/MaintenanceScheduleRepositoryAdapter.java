package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.retail.internal.application.port.MaintenanceScheduleRepository;
import com.gymmate.retail.internal.domain.MaintenanceSchedule;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link MaintenanceScheduleRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the MaintenanceSchedule finders live here.
 */
@Component
@Transactional()
public class MaintenanceScheduleRepositoryAdapter extends JpaDomainRepositoryAdapter<MaintenanceSchedule, UUID, MaintenanceScheduleJpaRepository>
        implements MaintenanceScheduleRepository {

    public MaintenanceScheduleRepositoryAdapter(MaintenanceScheduleJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
    public long countByEquipmentId(UUID equipmentId) {
        return jpaRepository.countByEquipmentId(equipmentId);
    }
}
