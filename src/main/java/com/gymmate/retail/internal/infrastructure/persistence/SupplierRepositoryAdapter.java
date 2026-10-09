package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.retail.internal.application.port.SupplierRepository;
import com.gymmate.retail.internal.domain.Supplier;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link SupplierRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the Supplier finders live here.
 */
@Component
@Transactional()
public class SupplierRepositoryAdapter extends JpaDomainRepositoryAdapter<Supplier, UUID, SupplierJpaRepository>
        implements SupplierRepository {

    public SupplierRepositoryAdapter(SupplierJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<Supplier> findByOrganisationId(UUID organisationId) {
        return this.<List<Supplier>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public List<Supplier> findActiveByOrganisationId(UUID organisationId) {
        return this.<List<Supplier>>fromJpa(jpaRepository.findActiveByOrganisationId(organisationId));
    }

    @Override
    public List<Supplier> findPreferredByOrganisationId(UUID organisationId) {
        return this.<List<Supplier>>fromJpa(jpaRepository.findPreferredByOrganisationId(organisationId));
    }

    @Override
    public Optional<Supplier> findByCode(String code) {
        return this.<Optional<Supplier>>fromJpa(jpaRepository.findByCode(code));
    }

    @Override
    public Optional<Supplier> findByOrganisationIdAndName(UUID organisationId, String name) {
        return this.<Optional<Supplier>>fromJpa(jpaRepository.findByOrganisationIdAndName(organisationId, name));
    }

    @Override
    public long countByOrganisationId(UUID organisationId) {
        return jpaRepository.countByOrganisationId(organisationId);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }
}
