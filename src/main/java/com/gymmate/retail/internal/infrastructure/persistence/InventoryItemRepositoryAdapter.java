package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.retail.internal.application.port.InventoryItemRepository;
import com.gymmate.retail.internal.domain.InventoryItem;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link InventoryItemRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the InventoryItem finders live here.
 */
@Component
@Transactional()
public class InventoryItemRepositoryAdapter extends JpaDomainRepositoryAdapter<InventoryItem, UUID, InventoryItemJpaRepository>
        implements InventoryItemRepository {

    public InventoryItemRepositoryAdapter(InventoryItemJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<InventoryItem> findByOrganisationId(UUID organisationId) {
        return this.<List<InventoryItem>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public List<InventoryItem> findByGymId(UUID gymId) {
        return this.<List<InventoryItem>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public Optional<InventoryItem> findBySku(String sku) {
        return this.<Optional<InventoryItem>>fromJpa(jpaRepository.findBySku(sku));
    }

    @Override
    public List<InventoryItem> findActiveByGymId(UUID gymId) {
        return this.<List<InventoryItem>>fromJpa(jpaRepository.findActiveByGymId(gymId));
    }

    @Override
    public List<InventoryItem> findActiveByOrganisationId(UUID organisationId) {
        return this.<List<InventoryItem>>fromJpa(jpaRepository.findActiveByOrganisationId(organisationId));
    }

    @Override
    public List<InventoryItem> findLowStockByGymId(UUID gymId) {
        return this.<List<InventoryItem>>fromJpa(jpaRepository.findLowStockByGymId(gymId));
    }

    @Override
    public List<InventoryItem> findLowStockByOrganisationId(UUID organisationId) {
        return this.<List<InventoryItem>>fromJpa(jpaRepository.findLowStockByOrganisationId(organisationId));
    }

    @Override
    public List<InventoryItem> findReorderNeededByGymId(UUID gymId) {
        return this.<List<InventoryItem>>fromJpa(jpaRepository.findReorderNeededByGymId(gymId));
    }

    @Override
    public List<InventoryItem> findReorderNeededByOrganisationId(UUID organisationId) {
        return this.<List<InventoryItem>>fromJpa(jpaRepository.findReorderNeededByOrganisationId(organisationId));
    }

    @Override
    public long countByGymId(UUID gymId) {
        return jpaRepository.countByGymId(gymId);
    }

    @Override
    public long countByOrganisationId(UUID organisationId) {
        return jpaRepository.countByOrganisationId(organisationId);
    }

    @Override
    public boolean existsBySku(String sku) {
        return jpaRepository.existsBySku(sku);
    }

    @Override
    public long countByGymIdAndCurrentStockLessThanMinimumStock(UUID gymId) {
        return jpaRepository.countByGymIdAndCurrentStockLessThanMinimumStock(gymId);
    }
}
