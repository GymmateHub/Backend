package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.retail.internal.application.port.EquipmentRepository;
import com.gymmate.retail.internal.domain.Equipment;
import com.gymmate.retail.internal.domain.EquipmentStatus;
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
 * Persistence adapter implementing {@link EquipmentRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class EquipmentRepositoryAdapter extends DomainRepositoryAdapter implements EquipmentRepository {

    private final EquipmentJpaRepository jpaRepository;

    public EquipmentRepositoryAdapter(EquipmentJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Equipment save(Equipment equipment) {
        return save(jpaRepository, equipment);
    }

    @Override
    public Optional<Equipment> findById(UUID id) {
        return this.<Optional<Equipment>>fromJpa(jpaRepository.findById(id));
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
    public void delete(Equipment equipment) {
        delete(jpaRepository, equipment);
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

    @Override
    public List<Equipment> saveAll(Iterable<Equipment> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Equipment> findAll() {
        return this.<List<Equipment>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<Equipment> findAllById(Iterable<UUID> ids) {
        return this.<List<Equipment>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<Equipment> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public Equipment saveAndFlush(Equipment entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<Equipment> findAll(Pageable pageable) {
        return this.<Page<Equipment>>fromJpa(jpaRepository.findAll(pageable));
    }
}
