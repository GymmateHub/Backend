package com.gymmate.billing.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.billing.internal.domain.SubscriptionTier;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.billing.internal.application.port.SubscriptionTierRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link SubscriptionTierRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the SubscriptionTier finders live here.
 */
@Component()
@Transactional()
public class SubscriptionTierRepositoryAdapter extends JpaDomainRepositoryAdapter<SubscriptionTier, UUID, SubscriptionTierJpaRepository>
        implements SubscriptionTierRepository {

    public SubscriptionTierRepositoryAdapter(SubscriptionTierJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public Optional<SubscriptionTier> findByName(String name) {
        return this.<Optional<SubscriptionTier>>fromJpa(jpaRepository.findByName(name));
    }

    @Override
    public List<SubscriptionTier> findByActiveTrueOrderBySortOrder() {
        return this.<List<SubscriptionTier>>fromJpa(jpaRepository.findByActiveTrueOrderBySortOrder());
    }

    @Override
    public List<SubscriptionTier> findFeaturedTiers() {
        return this.<List<SubscriptionTier>>fromJpa(jpaRepository.findFeaturedTiers());
    }

    @Override
    public List<SubscriptionTier> findSuitableTiersForMemberCount(Integer memberCount) {
        return this.<List<SubscriptionTier>>fromJpa(jpaRepository.findSuitableTiersForMemberCount(memberCount));
    }
}
