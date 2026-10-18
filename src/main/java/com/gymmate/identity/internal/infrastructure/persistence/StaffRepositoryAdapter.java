package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.identity.internal.domain.Staff;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.identity.internal.application.port.StaffRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link StaffRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the Staff finders live here.
 */
@Component()
@Transactional()
public class StaffRepositoryAdapter extends JpaDomainRepositoryAdapter<Staff, UUID, StaffJpaRepository>
        implements StaffRepository {

    public StaffRepositoryAdapter(StaffJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
