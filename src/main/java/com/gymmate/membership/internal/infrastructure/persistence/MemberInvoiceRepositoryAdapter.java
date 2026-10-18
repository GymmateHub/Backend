package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.membership.internal.domain.MemberInvoice;
import com.gymmate.membership.internal.domain.MemberInvoiceStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.membership.internal.application.port.MemberInvoiceRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link MemberInvoiceRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the MemberInvoice finders live here.
 */
@Component()
@Transactional()
public class MemberInvoiceRepositoryAdapter extends JpaDomainRepositoryAdapter<MemberInvoice, UUID, MemberInvoiceJpaRepository>
        implements MemberInvoiceRepository {

    public MemberInvoiceRepositoryAdapter(MemberInvoiceJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
