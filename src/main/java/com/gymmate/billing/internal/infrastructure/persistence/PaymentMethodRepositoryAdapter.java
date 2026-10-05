package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.billing.internal.domain.PaymentMethod;
import com.gymmate.shared.constants.PaymentMethodOwnerType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.billing.internal.application.port.PaymentMethodRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link PaymentMethodRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class PaymentMethodRepositoryAdapter extends DomainRepositoryAdapter implements PaymentMethodRepository {

    private final PaymentMethodJpaRepository jpaRepository;

    public PaymentMethodRepositoryAdapter(PaymentMethodJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Optional<PaymentMethod> findByProviderPaymentMethodId(String providerPaymentMethodId) {
        return this.<Optional<PaymentMethod>>fromJpa(jpaRepository.findByProviderPaymentMethodId(providerPaymentMethodId));
    }

    @Override
    public List<PaymentMethod> findByOwnerTypeAndOwnerIdOrderByIsDefaultDescCreatedAtDesc(PaymentMethodOwnerType ownerType, UUID ownerId) {
        return this.<List<PaymentMethod>>fromJpa(jpaRepository.findByOwnerTypeAndOwnerIdOrderByIsDefaultDescCreatedAtDesc(ownerType, ownerId));
    }

    @Override
    public Optional<PaymentMethod> findByOwnerTypeAndOwnerIdAndIsDefaultTrue(PaymentMethodOwnerType ownerType, UUID ownerId) {
        return this.<Optional<PaymentMethod>>fromJpa(jpaRepository.findByOwnerTypeAndOwnerIdAndIsDefaultTrue(ownerType, ownerId));
    }

    @Override
    public void clearDefaultForOwner(PaymentMethodOwnerType ownerType, UUID ownerId) {
        jpaRepository.clearDefaultForOwner(ownerType, ownerId);
    }

    @Override
    public boolean existsByOwnerTypeAndOwnerId(PaymentMethodOwnerType ownerType, UUID ownerId) {
        return jpaRepository.existsByOwnerTypeAndOwnerId(ownerType, ownerId);
    }

    @Override
    public void deleteByOwnerTypeAndOwnerIdAndId(PaymentMethodOwnerType ownerType, UUID ownerId, UUID id) {
        jpaRepository.deleteByOwnerTypeAndOwnerIdAndId(ownerType, ownerId, id);
    }

    @Override
    public List<PaymentMethod> findByOrganisationId(UUID organisationId) {
        return this.<List<PaymentMethod>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public Optional<PaymentMethod> findDefaultForOrganisation(UUID organisationId) {
        return this.<Optional<PaymentMethod>>fromJpa(jpaRepository.findDefaultForOrganisation(organisationId));
    }

    @Override
    public void clearDefaultForOrganisation(UUID organisationId) {
        jpaRepository.clearDefaultForOrganisation(organisationId);
    }

    @Override
    public boolean existsForOrganisation(UUID organisationId) {
        return jpaRepository.existsForOrganisation(organisationId);
    }

    @Override
    public List<PaymentMethod> findMemberPaymentMethodsByGym(UUID gymId) {
        return this.<List<PaymentMethod>>fromJpa(jpaRepository.findMemberPaymentMethodsByGym(gymId));
    }

    @Override
    public long countActiveByOwnerType(PaymentMethodOwnerType ownerType) {
        return jpaRepository.countActiveByOwnerType(ownerType);
    }

    @Override
    public long countActiveByGym(UUID gymId) {
        return jpaRepository.countActiveByGym(gymId);
    }

    @Override
    public List<Object[]> countByMethodType() {
        return jpaRepository.countByMethodType();
    }

    @Override
    public List<Object[]> countByCardBrand() {
        return jpaRepository.countByCardBrand();
    }

    @Override
    public PaymentMethod save(PaymentMethod entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<PaymentMethod> saveAll(Iterable<PaymentMethod> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<PaymentMethod> findById(UUID id) {
        return this.<Optional<PaymentMethod>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<PaymentMethod> findAll() {
        return this.<List<PaymentMethod>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<PaymentMethod> findAllById(Iterable<UUID> ids) {
        return this.<List<PaymentMethod>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(PaymentMethod entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<PaymentMethod> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public PaymentMethod saveAndFlush(PaymentMethod entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<PaymentMethod> findAll(Pageable pageable) {
        return this.<Page<PaymentMethod>>fromJpa(jpaRepository.findAll(pageable));
    }
}
