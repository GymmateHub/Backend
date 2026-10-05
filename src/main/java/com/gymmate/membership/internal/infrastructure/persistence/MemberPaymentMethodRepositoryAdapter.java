package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.membership.internal.domain.MemberPaymentMethod;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.membership.internal.application.port.MemberPaymentMethodRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link MemberPaymentMethodRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class MemberPaymentMethodRepositoryAdapter extends DomainRepositoryAdapter implements MemberPaymentMethodRepository {

    private final MemberPaymentMethodJpaRepository jpaRepository;

    public MemberPaymentMethodRepositoryAdapter(MemberPaymentMethodJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
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

    @Override
    public MemberPaymentMethod save(MemberPaymentMethod entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<MemberPaymentMethod> saveAll(Iterable<MemberPaymentMethod> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<MemberPaymentMethod> findById(UUID id) {
        return this.<Optional<MemberPaymentMethod>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<MemberPaymentMethod> findAll() {
        return this.<List<MemberPaymentMethod>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<MemberPaymentMethod> findAllById(Iterable<UUID> ids) {
        return this.<List<MemberPaymentMethod>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(MemberPaymentMethod entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<MemberPaymentMethod> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public MemberPaymentMethod saveAndFlush(MemberPaymentMethod entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<MemberPaymentMethod> findAll(Pageable pageable) {
        return this.<Page<MemberPaymentMethod>>fromJpa(jpaRepository.findAll(pageable));
    }
}
