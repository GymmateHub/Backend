package com.gymmate.shared.infrastructure.persistence;

import com.gymmate.shared.application.port.DomainRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

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
        return fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(ID id) {
        return jpaRepository.existsById(id);
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
        return fromJpa(jpaRepository.findAllById(ids));
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(ID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void delete(D aggregate) {
        delete(jpaRepository, aggregate);
    }
}
