package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.billing.internal.domain.RefundAuditLog;
import java.util.List;
import java.util.UUID;
import com.gymmate.billing.internal.application.port.RefundAuditLogRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link RefundAuditLogRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the RefundAuditLog finders live here.
 */
@Component()
@Transactional()
public class RefundAuditLogRepositoryAdapter extends JpaDomainRepositoryAdapter<RefundAuditLog, UUID, RefundAuditLogJpaRepository>
        implements RefundAuditLogRepository {

    public RefundAuditLogRepositoryAdapter(RefundAuditLogJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
