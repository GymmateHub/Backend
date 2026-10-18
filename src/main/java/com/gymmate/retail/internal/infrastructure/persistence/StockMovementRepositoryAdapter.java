package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.retail.internal.application.port.StockMovementRepository;
import com.gymmate.retail.internal.domain.MovementType;
import com.gymmate.retail.internal.domain.StockMovement;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link StockMovementRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the StockMovement finders live here.
 */
@Component
@Transactional()
public class StockMovementRepositoryAdapter extends JpaDomainRepositoryAdapter<StockMovement, UUID, StockMovementJpaRepository>
        implements StockMovementRepository {

    public StockMovementRepositoryAdapter(StockMovementJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<StockMovement> findByInventoryItemId(UUID inventoryItemId) {
        return this.<List<StockMovement>>fromJpa(jpaRepository.findByInventoryItemId(inventoryItemId));
    }

    @Override
    public List<StockMovement> findByGymId(UUID gymId) {
        return this.<List<StockMovement>>fromJpa(jpaRepository.findByGymId(gymId));
    }

    @Override
    public List<StockMovement> findByOrganisationId(UUID organisationId) {
        return this.<List<StockMovement>>fromJpa(jpaRepository.findByOrganisationId(organisationId));
    }

    @Override
    public List<StockMovement> findByInventoryItemIdOrderByMovementDateDesc(UUID inventoryItemId) {
        return this.<List<StockMovement>>fromJpa(jpaRepository.findByInventoryItemIdOrderByMovementDateDesc(inventoryItemId));
    }

    @Override
    public List<StockMovement> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return this.<List<StockMovement>>fromJpa(jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate));
    }

    @Override
    public List<StockMovement> findByOrganisationIdAndDateRange(UUID organisationId, LocalDateTime startDate, LocalDateTime endDate) {
        return this.<List<StockMovement>>fromJpa(jpaRepository.findByOrganisationIdAndDateRange(organisationId, startDate, endDate));
    }

    @Override
    public List<StockMovement> findByGymIdAndMovementType(UUID gymId, MovementType movementType) {
        return this.<List<StockMovement>>fromJpa(jpaRepository.findByGymIdAndMovementType(gymId, movementType));
    }

    @Override
    public List<StockMovement> findBySupplierId(UUID supplierId) {
        return this.<List<StockMovement>>fromJpa(jpaRepository.findBySupplierId(supplierId));
    }

    @Override
    public long countByInventoryItemId(UUID inventoryItemId) {
        return jpaRepository.countByInventoryItemId(inventoryItemId);
    }
}
