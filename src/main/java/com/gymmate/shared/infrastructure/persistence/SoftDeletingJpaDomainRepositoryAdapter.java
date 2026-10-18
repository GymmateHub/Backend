package com.gymmate.shared.infrastructure.persistence;

import com.gymmate.shared.domain.BaseAuditEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

/**
 * {@link JpaDomainRepositoryAdapter} for aggregates that are never physically removed: every
 * delete path ({@link #delete} and {@link #deleteById}) deactivates the aggregate instead, so
 * the row and its history are kept.
 *
 * @param <D>  domain aggregate type (carries the {@code active} flag)
 * @param <ID> identifier type
 * @param <R>  the module's Spring Data repository of the aggregate's JPA entity
 */
@Transactional
public abstract class SoftDeletingJpaDomainRepositoryAdapter<D extends BaseAuditEntity, ID, R extends JpaRepository<?, ID>>
        extends JpaDomainRepositoryAdapter<D, ID, R> {

    protected SoftDeletingJpaDomainRepositoryAdapter(R jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    /** Soft delete: marks the aggregate inactive and saves it. */
    @Override
    public void delete(D aggregate) {
        aggregate.deactivate();
        save(aggregate);
    }

    /** Soft delete by id; a missing id is ignored, as with a hard delete. */
    @Override
    public void deleteById(ID id) {
        findById(id).ifPresent(this::delete);
    }
}
