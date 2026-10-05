package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.retail.internal.application.port.SupplierRepository;
import com.gymmate.retail.internal.domain.Supplier;
import org.springframework.stereotype.Component;
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
 * Persistence adapter implementing {@link SupplierRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class SupplierRepositoryAdapter extends DomainRepositoryAdapter implements SupplierRepository {

    private final SupplierJpaRepository jpaRepository;

    public SupplierRepositoryAdapter(SupplierJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Supplier save(Supplier supplier) {
        return save(jpaRepository, supplier);
    }

    @Override
    public Optional<Supplier> findById(UUID id) {
        return this.<Optional<Supplier>>fromJpa(jpaRepository.findById(id));
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
    public void delete(Supplier supplier) {
        delete(jpaRepository, supplier);
    }

    @Override
    public long countByOrganisationId(UUID organisationId) {
        return jpaRepository.countByOrganisationId(organisationId);
    }

    @Override
    public boolean existsByCode(String code) {
        return jpaRepository.existsByCode(code);
    }

    @Override
    public List<Supplier> saveAll(Iterable<Supplier> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Supplier> findAll() {
        return this.<List<Supplier>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<Supplier> findAllById(Iterable<UUID> ids) {
        return this.<List<Supplier>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<Supplier> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public Supplier saveAndFlush(Supplier entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<Supplier> findAll(Pageable pageable) {
        return this.<Page<Supplier>>fromJpa(jpaRepository.findAll(pageable));
    }
}
