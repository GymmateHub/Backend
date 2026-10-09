package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.billing.internal.domain.PaymentMethod;
import com.gymmate.shared.constants.PaymentMethodOwnerType;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.billing.internal.application.port.PaymentMethodRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link PaymentMethodRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the PaymentMethod finders live here.
 */
@Component()
@Transactional()
public class PaymentMethodRepositoryAdapter extends JpaDomainRepositoryAdapter<PaymentMethod, UUID, PaymentMethodJpaRepository>
        implements PaymentMethodRepository {

    public PaymentMethodRepositoryAdapter(PaymentMethodJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
