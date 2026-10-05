package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.retail.internal.domain.SaleItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.retail.internal.application.port.SaleItemRepository;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link SaleItemRepository} with Spring Data JPA.
 */
@Component()
@Transactional()
public class SaleItemRepositoryAdapter extends DomainRepositoryAdapter implements SaleItemRepository {

    private final SaleItemJpaRepository jpaRepository;

    public SaleItemRepositoryAdapter(SaleItemJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public List<SaleItem> findBySaleId(UUID saleId) {
        return this.<List<SaleItem>>fromJpa(jpaRepository.findBySaleId(saleId));
    }

    @Override
    public List<SaleItem> findByInventoryItemId(UUID inventoryItemId) {
        return this.<List<SaleItem>>fromJpa(jpaRepository.findByInventoryItemId(inventoryItemId));
    }

    @Override
    public List<SaleItem> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return this.<List<SaleItem>>fromJpa(jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate));
    }

    @Override
    public List<Object[]> findTopSellingItems(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findTopSellingItems(gymId, startDate, endDate);
    }

    @Override
    public long countItemsSoldByGymId(UUID gymId) {
        return jpaRepository.countItemsSoldByGymId(gymId);
    }

    @Override
    public SaleItem save(SaleItem entity) {
        return save(jpaRepository, entity);
    }

    @Override
    public List<SaleItem> saveAll(Iterable<SaleItem> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public Optional<SaleItem> findById(UUID id) {
        return this.<Optional<SaleItem>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<SaleItem> findAll() {
        return this.<List<SaleItem>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<SaleItem> findAllById(Iterable<UUID> ids) {
        return this.<List<SaleItem>>fromJpa(jpaRepository.findAllById(ids));
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
    public void delete(SaleItem entity) {
        delete(jpaRepository, entity);
    }

    @Override
    public void deleteAll(Iterable<SaleItem> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public SaleItem saveAndFlush(SaleItem entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<SaleItem> findAll(Pageable pageable) {
        return this.<Page<SaleItem>>fromJpa(jpaRepository.findAll(pageable));
    }
}
