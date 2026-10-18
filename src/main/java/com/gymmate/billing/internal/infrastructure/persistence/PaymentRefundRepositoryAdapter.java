package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.billing.internal.domain.PaymentRefund;
import com.gymmate.shared.constants.RefundStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.billing.internal.application.port.PaymentRefundRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link PaymentRefundRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the PaymentRefund finders live here.
 */
@Component()
@Transactional()
public class PaymentRefundRepositoryAdapter extends JpaDomainRepositoryAdapter<PaymentRefund, UUID, PaymentRefundJpaRepository>
        implements PaymentRefundRepository {

    public PaymentRefundRepositoryAdapter(PaymentRefundJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
