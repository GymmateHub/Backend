package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.billing.internal.domain.RefundAuditLog;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.billing.internal.application.port.RefundAuditLogRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link RefundAuditLogRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class RefundAuditLogRepositoryAdapter extends DomainRepositoryAdapter implements RefundAuditLogRepository {

    private final RefundAuditLogJpaRepository jpaRepository;

    public RefundAuditLogRepositoryAdapter(RefundAuditLogJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<RefundAuditLog> findByRefundRequestIdOrderByCreatedAtAsc(UUID refundRequestId) {
        return this.<List<RefundAuditLog>>fromJpa(jpaRepository.findByRefundRequestIdOrderByCreatedAtAsc(refundRequestId));
    }

    @Override
    public List<RefundAuditLog> findByPaymentRefundIdOrderByCreatedAtAsc(UUID paymentRefundId) {
        return this.<List<RefundAuditLog>>fromJpa(jpaRepository.findByPaymentRefundIdOrderByCreatedAtAsc(paymentRefundId));
    }

    @Override
    public List<RefundAuditLog> findByActionOrderByCreatedAtDesc(String action) {
        return this.<List<RefundAuditLog>>fromJpa(jpaRepository.findByActionOrderByCreatedAtDesc(action));
    }

    @Override
    public List<RefundAuditLog> findByPerformedByUserIdOrderByCreatedAtDesc(UUID userId) {
        return this.<List<RefundAuditLog>>fromJpa(jpaRepository.findByPerformedByUserIdOrderByCreatedAtDesc(userId));
    }

    @Override
    public RefundAuditLog save(RefundAuditLog entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<RefundAuditLog> saveAll(Iterable<RefundAuditLog> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<RefundAuditLog> findById(UUID id) {
        return this.<Optional<RefundAuditLog>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<RefundAuditLog> findAll() {
        return this.<List<RefundAuditLog>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<RefundAuditLog> findAllById(Iterable<UUID> ids) {
        return this.<List<RefundAuditLog>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(RefundAuditLog entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<RefundAuditLog> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public RefundAuditLog saveAndFlush(RefundAuditLog entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<RefundAuditLog> findAll(Pageable pageable) {
        return this.<Page<RefundAuditLog>>fromJpa(jpaRepository.findAll(pageable));
    }
}
