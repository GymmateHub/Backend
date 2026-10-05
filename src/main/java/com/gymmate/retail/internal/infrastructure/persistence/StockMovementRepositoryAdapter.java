package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.retail.internal.application.port.StockMovementRepository;
import com.gymmate.retail.internal.domain.MovementType;
import com.gymmate.retail.internal.domain.StockMovement;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
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
 * Persistence adapter implementing {@link StockMovementRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class StockMovementRepositoryAdapter extends DomainRepositoryAdapter implements StockMovementRepository {

    private final StockMovementJpaRepository jpaRepository;

    public StockMovementRepositoryAdapter(StockMovementJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public StockMovement save(StockMovement stockMovement) {
        return save(jpaRepository, stockMovement);
    }

    @Override
    public Optional<StockMovement> findById(UUID id) {
        return this.<Optional<StockMovement>>fromJpa(jpaRepository.findById(id));
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
    public void delete(StockMovement stockMovement) {
        delete(jpaRepository, stockMovement);
    }

    @Override
    public long countByInventoryItemId(UUID inventoryItemId) {
        return jpaRepository.countByInventoryItemId(inventoryItemId);
    }

    @Override
    public List<StockMovement> saveAll(Iterable<StockMovement> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<StockMovement> findAll() {
        return this.<List<StockMovement>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<StockMovement> findAllById(Iterable<UUID> ids) {
        return this.<List<StockMovement>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<StockMovement> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public StockMovement saveAndFlush(StockMovement entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<StockMovement> findAll(Pageable pageable) {
        return this.<Page<StockMovement>>fromJpa(jpaRepository.findAll(pageable));
    }
}
