package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.retail.internal.application.port.InventoryItemRepository;
import com.gymmate.retail.internal.domain.InventoryItem;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link InventoryItemRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class InventoryItemRepositoryAdapter extends DomainRepositoryAdapter implements InventoryItemRepository {

    private final InventoryItemJpaRepository jpaRepository;

    public InventoryItemRepositoryAdapter(InventoryItemJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public InventoryItem save(InventoryItem inventoryItem) {
        return save(jpaRepository, inventoryItem);
    }

    @Override
    public Optional<InventoryItem> findById(UUID id) {
        return this.<Optional<InventoryItem>>fromJpa(jpaRepository.findById(id));
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
    public void delete(InventoryItem inventoryItem) {
        delete(jpaRepository, inventoryItem);
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

    @Override
    public List<InventoryItem> saveAll(Iterable<InventoryItem> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<InventoryItem> findAll() {
        return this.<List<InventoryItem>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<InventoryItem> findAllById(Iterable<UUID> ids) {
        return this.<List<InventoryItem>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<InventoryItem> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public InventoryItem saveAndFlush(InventoryItem entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<InventoryItem> findAll(Pageable pageable) {
        return this.<Page<InventoryItem>>fromJpa(jpaRepository.findAll(pageable));
    }
}
