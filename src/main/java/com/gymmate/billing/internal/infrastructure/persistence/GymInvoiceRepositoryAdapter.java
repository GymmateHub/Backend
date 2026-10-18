package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.billing.internal.domain.GymInvoice;
import com.gymmate.shared.constants.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.billing.internal.application.port.GymInvoiceRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link GymInvoiceRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the GymInvoice finders live here.
 */
@Component()
@Transactional()
public class GymInvoiceRepositoryAdapter extends JpaDomainRepositoryAdapter<GymInvoice, UUID, GymInvoiceJpaRepository>
        implements GymInvoiceRepository {

    public GymInvoiceRepositoryAdapter(GymInvoiceJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<GymInvoice> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId) {
        return this.<List<GymInvoice>>fromJpa(jpaRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId));
    }

    @Override
    public List<GymInvoice> findByOrganisationIdAndStatus(UUID organisationId, InvoiceStatus status) {
        return this.<List<GymInvoice>>fromJpa(jpaRepository.findByOrganisationIdAndStatus(organisationId, status));
    }

    @Override
    public BigDecimal sumPaidAmountByOrganisationIdAndPeriod(UUID orgId, LocalDateTime start, LocalDateTime end) {
        return jpaRepository.sumPaidAmountByOrganisationIdAndPeriod(orgId, start, end);
    }

    @Override
    public Optional<GymInvoice> findByStripeInvoiceId(String stripeInvoiceId) {
        return this.<Optional<GymInvoice>>fromJpa(jpaRepository.findByStripeInvoiceId(stripeInvoiceId));
    }
}
