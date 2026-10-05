package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.billing.internal.domain.PaymentRefund;
import com.gymmate.shared.constants.RefundStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.billing.internal.application.port.PaymentRefundRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link PaymentRefundRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class PaymentRefundRepositoryAdapter extends DomainRepositoryAdapter implements PaymentRefundRepository {

    private final PaymentRefundJpaRepository jpaRepository;

    public PaymentRefundRepositoryAdapter(PaymentRefundJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<PaymentRefund> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId) {
        return this.<List<PaymentRefund>>fromJpa(jpaRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId));
    }

    @Override
    public List<PaymentRefund> findByOrganisationIdAndStatusOrderByCreatedAtDesc(UUID organisationId, RefundStatus status) {
        return this.<List<PaymentRefund>>fromJpa(jpaRepository.findByOrganisationIdAndStatusOrderByCreatedAtDesc(organisationId, status));
    }

    @Override
    public long countByOrganisationIdAndStatus(UUID organisationId, RefundStatus status) {
        return jpaRepository.countByOrganisationIdAndStatus(organisationId, status);
    }

    @Override
    public List<PaymentRefund> findByOrganisationIdAndDateRange(UUID organisationId, LocalDateTime startDate, LocalDateTime endDate) {
        return this.<List<PaymentRefund>>fromJpa(jpaRepository.findByOrganisationIdAndDateRange(organisationId, startDate, endDate));
    }

    @Override
    public BigDecimal sumRefundAmountByOrganisationIdAndDateRange(UUID organisationId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.sumRefundAmountByOrganisationIdAndDateRange(organisationId, startDate, endDate);
    }

    @Override
    public Optional<PaymentRefund> findByStripeRefundId(String stripeRefundId) {
        return this.<Optional<PaymentRefund>>fromJpa(jpaRepository.findByStripeRefundId(stripeRefundId));
    }

    @Override
    public List<PaymentRefund> findByStripePaymentIntentIdOrderByCreatedAtDesc(String stripePaymentIntentId) {
        return this.<List<PaymentRefund>>fromJpa(jpaRepository.findByStripePaymentIntentIdOrderByCreatedAtDesc(stripePaymentIntentId));
    }

    @Override
    public PaymentRefund save(PaymentRefund entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<PaymentRefund> saveAll(Iterable<PaymentRefund> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<PaymentRefund> findById(UUID id) {
        return this.<Optional<PaymentRefund>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<PaymentRefund> findAll() {
        return this.<List<PaymentRefund>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<PaymentRefund> findAllById(Iterable<UUID> ids) {
        return this.<List<PaymentRefund>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(PaymentRefund entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<PaymentRefund> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public PaymentRefund saveAndFlush(PaymentRefund entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<PaymentRefund> findAll(Pageable pageable) {
        return this.<Page<PaymentRefund>>fromJpa(jpaRepository.findAll(pageable));
    }
}
