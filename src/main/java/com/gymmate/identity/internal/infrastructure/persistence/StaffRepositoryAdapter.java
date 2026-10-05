package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.identity.internal.domain.Staff;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.identity.internal.application.port.StaffRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link StaffRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class StaffRepositoryAdapter extends DomainRepositoryAdapter implements StaffRepository {

    private final StaffJpaRepository jpaRepository;

    public StaffRepositoryAdapter(StaffJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<Staff> findByUserId(UUID userId) {
        return this.<Optional<Staff>>fromJpa(jpaRepository.findByUserId(userId));
    }

    @Override
    public boolean existsByUserId(UUID userId) {
        return jpaRepository.existsByUserId(userId);
    }

    @Override
    public List<Staff> findByOrganisationId(UUID organisationId) {
        return this.<List<Staff>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public List<Staff> findAllActiveByOrganisationId(UUID organisationId) {
        return this.<List<Staff>>fromJpa(jpaRepository.findAllActiveByOrganisationId(organisationId));
    }

    @Override
    public List<Staff> findByOrganisationIdAndDepartment(UUID organisationId, String department) {
        return this.<List<Staff>>fromJpa(jpaRepository.findByOrganisationIdAndDepartment(organisationId, department));
    }

    @Override
    public List<Staff> findByOrganisationIdAndPosition(UUID organisationId, String position) {
        return this.<List<Staff>>fromJpa(jpaRepository.findByOrganisationIdAndPosition(organisationId, position));
    }

    @Override
    public List<Staff> findByOrganisationIdAndEmploymentType(UUID organisationId, String employmentType) {
        return this.<List<Staff>>fromJpa(jpaRepository.findByOrganisationIdAndEmploymentType(organisationId, employmentType));
    }

    @Override
    public List<Staff> findByDepartment(String department) {
        return this.<List<Staff>>fromJpa(jpaRepository.findByDepartment(department));
    }

    @Override
    public List<Staff> findByPosition(String position) {
        return this.<List<Staff>>fromJpa(jpaRepository.findByPosition(position));
    }

    @Override
    public List<Staff> findByEmploymentType(String employmentType) {
        return this.<List<Staff>>fromJpa(jpaRepository.findByEmploymentType(employmentType));
    }

    @Override
    public List<Staff> findAllActive() {
        return this.<List<Staff>>fromJpa(jpaRepository.findAllActive());
    }

    @Override
    public Staff save(Staff entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<Staff> saveAll(Iterable<Staff> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<Staff> findById(UUID id) {
        return this.<Optional<Staff>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Staff> findAll() {
        return this.<List<Staff>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<Staff> findAllById(Iterable<UUID> ids) {
        return this.<List<Staff>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(Staff entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<Staff> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public Staff saveAndFlush(Staff entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<Staff> findAll(Pageable pageable) {
        return this.<Page<Staff>>fromJpa(jpaRepository.findAll(pageable));
    }
}
