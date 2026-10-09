package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.billing.internal.domain.RefundRequestEntity;
import com.gymmate.shared.constants.RefundRequestStatus;
import com.gymmate.shared.constants.RefundType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.billing.internal.application.port.RefundRequestRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link RefundRequestRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the RefundRequestEntity finders live here.
 */
@Component()
@Transactional()
public class RefundRequestRepositoryAdapter extends JpaDomainRepositoryAdapter<RefundRequestEntity, UUID, RefundRequestJpaRepository>
        implements RefundRequestRepository {

    public RefundRequestRepositoryAdapter(RefundRequestJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<RefundRequestEntity> findByGymIdAndOrganisationIdOrderByCreatedAtDesc(UUID gymId, UUID organisationId) {
        return this.<List<RefundRequestEntity>>fromJpa(jpaRepository.findByGymIdAndOrganisationIdOrderByCreatedAtDesc(gymId, organisationId));
    }

    @Override
    public List<RefundRequestEntity> findByGymIdAndOrganisationIdAndStatusOrderByCreatedAtDesc(UUID gymId, UUID organisationId, RefundRequestStatus status) {
        return this.<List<RefundRequestEntity>>fromJpa(jpaRepository.findByGymIdAndOrganisationIdAndStatusOrderByCreatedAtDesc(gymId, organisationId, status));
    }

    @Override
    public List<RefundRequestEntity> findPendingByGymIdAndOrganisationId(UUID gymId, UUID organisationId) {
        return this.<List<RefundRequestEntity>>fromJpa(jpaRepository.findPendingByGymIdAndOrganisationId(gymId, organisationId));
    }

    @Override
    public List<RefundRequestEntity> findByRequestedByUserIdAndOrganisationIdOrderByCreatedAtDesc(UUID userId, UUID organisationId) {
        return this.<List<RefundRequestEntity>>fromJpa(jpaRepository.findByRequestedByUserIdAndOrganisationIdOrderByCreatedAtDesc(userId, organisationId));
    }

    @Override
    public List<RefundRequestEntity> findByRefundToUserIdAndOrganisationIdOrderByCreatedAtDesc(UUID userId, UUID organisationId) {
        return this.<List<RefundRequestEntity>>fromJpa(jpaRepository.findByRefundToUserIdAndOrganisationIdOrderByCreatedAtDesc(userId, organisationId));
    }

    @Override
    public Optional<RefundRequestEntity> findByStripePaymentIntentIdAndStatus(String paymentIntentId, RefundRequestStatus status) {
        return this.<Optional<RefundRequestEntity>>fromJpa(jpaRepository.findByStripePaymentIntentIdAndStatus(paymentIntentId, status));
    }

    @Override
    public List<RefundRequestEntity> findEscalatedRequests() {
        return this.<List<RefundRequestEntity>>fromJpa(jpaRepository.findEscalatedRequests());
    }

    @Override
    public List<RefundRequestEntity> findOverdueRequests(LocalDateTime now) {
        return this.<List<RefundRequestEntity>>fromJpa(jpaRepository.findOverdueRequests(now));
    }

    @Override
    public long countByGymIdAndOrganisationIdAndStatus(UUID gymId, UUID organisationId, RefundRequestStatus status) {
        return jpaRepository.countByGymIdAndOrganisationIdAndStatus(gymId, organisationId, status);
    }

    @Override
    public List<RefundRequestEntity> findByGymIdAndOrganisationIdAndRefundTypeOrderByCreatedAtDesc(UUID gymId, UUID organisationId, RefundType refundType) {
        return this.<List<RefundRequestEntity>>fromJpa(jpaRepository.findByGymIdAndOrganisationIdAndRefundTypeOrderByCreatedAtDesc(gymId, organisationId, refundType));
    }

    @Override
    public List<RefundRequestEntity> findPendingPlatformRefunds() {
        return this.<List<RefundRequestEntity>>fromJpa(jpaRepository.findPendingPlatformRefunds());
    }

    @Override
    public java.math.BigDecimal sumProcessedRefundsByGymIdAndOrganisationIdAndDateRange(UUID gymId, UUID organisationId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.sumProcessedRefundsByGymIdAndOrganisationIdAndDateRange(gymId, organisationId, startDate, endDate);
    }

    @Override
    public Optional<RefundRequestEntity> findByIdAndOrganisationId(UUID id, UUID organisationId) {
        return this.<Optional<RefundRequestEntity>>fromJpa(jpaRepository.findByIdAndOrganisationId(id, organisationId));
    }
}
