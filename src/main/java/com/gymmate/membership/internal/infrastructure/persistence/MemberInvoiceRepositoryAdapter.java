package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.membership.internal.domain.MemberInvoice;
import com.gymmate.membership.internal.domain.MemberInvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.membership.internal.application.port.MemberInvoiceRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link MemberInvoiceRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class MemberInvoiceRepositoryAdapter extends DomainRepositoryAdapter implements MemberInvoiceRepository {

    private final MemberInvoiceJpaRepository jpaRepository;

    public MemberInvoiceRepositoryAdapter(MemberInvoiceJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public BigDecimal sumPaidAmountByGymIdAndPeriod(UUID gymId, LocalDateTime start, LocalDateTime end) {
        return jpaRepository.sumPaidAmountByGymIdAndPeriod(gymId, start, end);
    }

    @Override
    public long countOverdueByGymId(UUID gymId, LocalDateTime now) {
        return jpaRepository.countOverdueByGymId(gymId, now);
    }

    @Override
    public List<MemberInvoice> findByMemberIdAndGymIdOrderByCreatedAtDesc(UUID memberId, UUID gymId) {
        return this.<List<MemberInvoice>>fromJpa(jpaRepository.findByMemberIdAndGymIdOrderByCreatedAtDesc(memberId, gymId));
    }

    @Override
    public List<MemberInvoice> findByMembershipIdOrderByCreatedAtDesc(UUID membershipId) {
        return this.<List<MemberInvoice>>fromJpa(jpaRepository.findByMembershipIdOrderByCreatedAtDesc(membershipId));
    }

    @Override
    public Optional<MemberInvoice> findByStripeInvoiceId(String stripeInvoiceId) {
        return this.<Optional<MemberInvoice>>fromJpa(jpaRepository.findByStripeInvoiceId(stripeInvoiceId));
    }

    @Override
    public List<MemberInvoice> findByMemberIdAndStatus(UUID memberId, MemberInvoiceStatus status) {
        return this.<List<MemberInvoice>>fromJpa(jpaRepository.findByMemberIdAndStatus(memberId, status));
    }

    @Override
    public MemberInvoice save(MemberInvoice entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<MemberInvoice> saveAll(Iterable<MemberInvoice> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<MemberInvoice> findById(UUID id) {
        return this.<Optional<MemberInvoice>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<MemberInvoice> findAll() {
        return this.<List<MemberInvoice>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<MemberInvoice> findAllById(Iterable<UUID> ids) {
        return this.<List<MemberInvoice>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(MemberInvoice entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<MemberInvoice> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public MemberInvoice saveAndFlush(MemberInvoice entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<MemberInvoice> findAll(Pageable pageable) {
        return this.<Page<MemberInvoice>>fromJpa(jpaRepository.findAll(pageable));
    }
}
