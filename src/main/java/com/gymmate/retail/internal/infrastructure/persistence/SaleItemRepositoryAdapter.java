package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.retail.internal.domain.SaleItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.gymmate.retail.internal.application.port.SaleItemRepository;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link SaleItemRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the SaleItem finders live here.
 */
@Component()
@Transactional()
public class SaleItemRepositoryAdapter extends JpaDomainRepositoryAdapter<SaleItem, UUID, SaleItemJpaRepository>
        implements SaleItemRepository {

    public SaleItemRepositoryAdapter(SaleItemJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
