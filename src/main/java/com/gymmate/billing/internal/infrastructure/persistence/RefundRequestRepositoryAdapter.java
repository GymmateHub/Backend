package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.billing.internal.domain.RefundRequestEntity;
import com.gymmate.shared.constants.RefundRequestStatus;
import com.gymmate.shared.constants.RefundType;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.billing.internal.application.port.RefundRequestRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link RefundRequestRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class RefundRequestRepositoryAdapter extends DomainRepositoryAdapter implements RefundRequestRepository {

    private final RefundRequestJpaRepository jpaRepository;

    public RefundRequestRepositoryAdapter(RefundRequestJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
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

    @Override
    public RefundRequestEntity save(RefundRequestEntity entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<RefundRequestEntity> saveAll(Iterable<RefundRequestEntity> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<RefundRequestEntity> findById(UUID id) {
        return this.<Optional<RefundRequestEntity>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<RefundRequestEntity> findAll() {
        return this.<List<RefundRequestEntity>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<RefundRequestEntity> findAllById(Iterable<UUID> ids) {
        return this.<List<RefundRequestEntity>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(RefundRequestEntity entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<RefundRequestEntity> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public RefundRequestEntity saveAndFlush(RefundRequestEntity entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<RefundRequestEntity> findAll(Pageable pageable) {
        return this.<Page<RefundRequestEntity>>fromJpa(jpaRepository.findAll(pageable));
    }
}
