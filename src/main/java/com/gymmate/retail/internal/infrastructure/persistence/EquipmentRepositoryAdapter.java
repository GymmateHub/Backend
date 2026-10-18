package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.retail.internal.application.port.EquipmentRepository;
import com.gymmate.retail.internal.domain.Equipment;
import com.gymmate.retail.internal.domain.EquipmentStatus;
import org.springframework.stereotype.Component;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link EquipmentRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the Equipment finders live here.
 */
@Component
@Transactional()
public class EquipmentRepositoryAdapter extends JpaDomainRepositoryAdapter<Equipment, UUID, EquipmentJpaRepository>
        implements EquipmentRepository {

    public EquipmentRepositoryAdapter(EquipmentJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<Equipment> findByOrganisationId(UUID organisationId) {
        return this.<List<Equipment>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public List<Equipment> findByGymId(UUID gymId) {
        return this.<List<Equipment>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<Equipment> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId) {
        return this.<List<Equipment>>fromJpa(jpaRepository.findByOrganisationIdAndGymId(organisationId, gymId));
    }

    @Override
    public List<Equipment> findActiveByOrganisationId(UUID organisationId) {
        return this.<List<Equipment>>fromJpa(jpaRepository.findActiveByOrganisationId(organisationId));
    }

    @Override
    public List<Equipment> findActiveByGymId(UUID gymId) {
        return this.<List<Equipment>>fromJpa(jpaRepository.findActiveByGymId(gymId));
    }

    @Override
    public List<Equipment> findByOrganisationIdAndStatus(UUID organisationId, EquipmentStatus status) {
        return this.<List<Equipment>>fromJpa(jpaRepository.findByOrganisationIdAndStatus(organisationId, status));
    }

    @Override
    public List<Equipment> findByGymIdAndStatus(UUID gymId, EquipmentStatus status) {
        return this.<List<Equipment>>fromJpa(jpaRepository.findByGymIdAndStatus(gymId, status));
    }

    @Override
    public List<Equipment> findMaintenanceDueByGymId(UUID gymId, LocalDate date) {
        return this.<List<Equipment>>fromJpa(jpaRepository.findMaintenanceDueByGymId(gymId, date));
    }

    @Override
    public List<Equipment> findMaintenanceDueByOrganisationId(UUID organisationId, LocalDate date) {
        return this.<List<Equipment>>fromJpa(jpaRepository.findMaintenanceDueByOrganisationId(organisationId, date));
    }

    @Override
    public Optional<Equipment> findBySerialNumber(String serialNumber) {
        return this.<Optional<Equipment>>fromJpa(jpaRepository.findBySerialNumber(serialNumber));
    }

    @Override
    public long countByGymId(UUID gymId) {
        return jpaRepository.countByGymId(gymId);
    }

    @Override
    public long countByOrganisationId(UUID organisationId) {
        return jpaRepository.countByOrganisationId(organisationId);
    }

    @Override
    public boolean existsBySerialNumber(String serialNumber) {
        return jpaRepository.existsBySerialNumber(serialNumber);
    }
}
