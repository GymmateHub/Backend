package com.gymmate.shared.infrastructure.persistence;

import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.shared.application.port.DomainRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.UUID;
import java.util.List;
import java.util.Optional;

/**
 * Generic implementation of {@link DomainRepository} on top of a Spring Data JPA repository.
 * Concrete adapters extend it, pass their {@code *JpaRepository} and implement only their
 * module-specific finders, using {@link #jpaRepository} (typed to their own Spring Data
 * interface) and {@link #fromJpa}.
 *
 * @param <D>  domain aggregate type
 * @param <ID> identifier type
 * @param <R>  the module's Spring Data repository of the aggregate's JPA entity
 *
 * <p>Id lookups ({@code findById}, {@code existsById}, {@code findAllById}, {@code deleteById})
 * treat a {@link TenantOwned} row of another organisation as absent. Hibernate's tenant filter
 * applies to queries but not to primary-key loads, so without this check any caller could read
 * or change another organisation's records by id. Calls without a tenant context (platform
 * admin, scheduled jobs, webhooks) are unaffected.
 */
@Transactional
public abstract class JpaDomainRepositoryAdapter<D, ID, R extends JpaRepository<?, ID>>
        extends DomainRepositoryAdapter implements DomainRepository<D, ID> {

    protected final R jpaRepository;

    protected JpaDomainRepositoryAdapter(R jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public D save(D aggregate) {
        return save(jpaRepository, aggregate);
    }

    @Override
    public List<D> saveAll(Iterable<D> aggregates) {
        return saveAll(jpaRepository, aggregates);
    }

    @Override
    public Optional<D> findById(ID id) {
        return fromJpa(visibleById(id));
    }

    @Override
    public boolean existsById(ID id) {
        return visibleById(id).isPresent();
    }

    @Override
    public List<D> findAll() {
        return fromJpa(jpaRepository.findAll());
    }

    @Override
    public Page<D> findAll(Pageable pageable) {
        return fromJpa(jpaRepository.findAll(pageable));
    }

    @Override
    public List<D> findAllById(Iterable<ID> ids) {
        List<Object> visible = new ArrayList<>();
        for (Object entity : jpaRepository.findAllById(ids)) {
            if (visibleToCurrentTenant(entity)) visible.add(entity);
        }
        return fromJpa(visible);
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(ID id) {
        if (visibleById(id).isPresent()) {
            jpaRepository.deleteById(id);
        }
    }

    @Override
    public void delete(D aggregate) {
        delete(jpaRepository, aggregate);
    }

    private Optional<Object> visibleById(ID id) {
        return jpaRepository.findById(id).map(Object.class::cast).filter(JpaDomainRepositoryAdapter::visibleToCurrentTenant);
    }

    static boolean visibleToCurrentTenant(Object entity) {
        if (!(entity instanceof TenantOwned owned)) return true;
        UUID tenant = TenantContext.getCurrentTenantId();
        return tenant == null || owned.getOrganisationId() == null || tenant.equals(owned.getOrganisationId());
    }
}
