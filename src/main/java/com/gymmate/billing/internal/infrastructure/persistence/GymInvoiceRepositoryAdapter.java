package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.billing.internal.domain.GymInvoice;
import com.gymmate.shared.constants.InvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.billing.internal.application.port.GymInvoiceRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link GymInvoiceRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class GymInvoiceRepositoryAdapter extends DomainRepositoryAdapter implements GymInvoiceRepository {

    private final GymInvoiceJpaRepository jpaRepository;

    public GymInvoiceRepositoryAdapter(GymInvoiceJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
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

    @Override
    public GymInvoice save(GymInvoice entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<GymInvoice> saveAll(Iterable<GymInvoice> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<GymInvoice> findById(UUID id) {
        return this.<Optional<GymInvoice>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<GymInvoice> findAll() {
        return this.<List<GymInvoice>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<GymInvoice> findAllById(Iterable<UUID> ids) {
        return this.<List<GymInvoice>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(GymInvoice entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<GymInvoice> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public GymInvoice saveAndFlush(GymInvoice entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<GymInvoice> findAll(Pageable pageable) {
        return this.<Page<GymInvoice>>fromJpa(jpaRepository.findAll(pageable));
    }
}
