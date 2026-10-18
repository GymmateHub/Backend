package com.gymmate.retail.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.retail.internal.domain.SaleItem;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/** Repository port for {@link SaleItem} aggregates. */
public interface SaleItemRepository extends DomainRepository<SaleItem, UUID> {

    List<SaleItem> findBySaleId(UUID saleId);

    List<SaleItem> findByInventoryItemId(UUID inventoryItemId);

    List<SaleItem> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

    List<Object[]> findTopSellingItems(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

    long countItemsSoldByGymId(UUID gymId);
}
