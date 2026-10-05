package com.gymmate.retail.internal.application.port;

import com.gymmate.retail.internal.domain.SaleItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Repository port for {@link SaleItem} aggregates. */
public interface SaleItemRepository {

    List<SaleItem> findBySaleId(UUID saleId);

    List<SaleItem> findByInventoryItemId(UUID inventoryItemId);

    List<SaleItem> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

    List<Object[]> findTopSellingItems(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

    long countItemsSoldByGymId(UUID gymId);

    SaleItem save(SaleItem entity);

    List<SaleItem> saveAll(Iterable<SaleItem> entities);

    Optional<SaleItem> findById(UUID id);

    boolean existsById(UUID id);

    List<SaleItem> findAll();

    List<SaleItem> findAllById(Iterable<UUID> ids);

    long count();

    void deleteById(UUID id);

    void delete(SaleItem entity);

    void deleteAll(Iterable<SaleItem> entities);

    SaleItem saveAndFlush(SaleItem entity);

    void flush();

    Page<SaleItem> findAll(Pageable pageable);
}
