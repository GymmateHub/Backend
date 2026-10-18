package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.membership.internal.domain.MemberPaymentMethod;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.membership.internal.application.port.MemberPaymentMethodRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link MemberPaymentMethodRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the MemberPaymentMethod finders live here.
 */
@Component()
@Transactional()
public class MemberPaymentMethodRepositoryAdapter extends JpaDomainRepositoryAdapter<MemberPaymentMethod, UUID, MemberPaymentMethodJpaRepository>
        implements MemberPaymentMethodRepository {

    public MemberPaymentMethodRepositoryAdapter(MemberPaymentMethodJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<MemberPaymentMethod> findByMemberIdAndGymIdOrderByIsDefaultDescCreatedAtDesc(UUID memberId, UUID gymId) {
        return this.<List<MemberPaymentMethod>>fromJpa(jpaRepository.findByMemberIdAndGymIdOrderByIsDefaultDescCreatedAtDesc(memberId, gymId));
    }

    @Override
    public Optional<MemberPaymentMethod> findByMemberIdAndGymIdAndIsDefaultTrue(UUID memberId, UUID gymId) {
        return this.<Optional<MemberPaymentMethod>>fromJpa(jpaRepository.findByMemberIdAndGymIdAndIsDefaultTrue(memberId, gymId));
    }

    @Override
    public Optional<MemberPaymentMethod> findByStripePaymentMethodId(String stripePaymentMethodId) {
        return this.<Optional<MemberPaymentMethod>>fromJpa(jpaRepository.findByStripePaymentMethodId(stripePaymentMethodId));
    }

    @Override
    public void clearDefaultForMember(UUID memberId, UUID gymId) {
        jpaRepository.clearDefaultForMember(memberId, gymId);
    }

    @Override
    public boolean existsByMemberIdAndGymId(UUID memberId, UUID gymId) {
        return jpaRepository.existsByMemberIdAndGymId(memberId, gymId);
    }
}
