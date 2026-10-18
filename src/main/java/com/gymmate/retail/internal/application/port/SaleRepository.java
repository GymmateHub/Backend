package com.gymmate.retail.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.retail.internal.domain.Sale;
import com.gymmate.retail.internal.domain.SaleStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/** Repository port for {@link Sale} aggregates. */
public interface SaleRepository extends DomainRepository<Sale, UUID> {

    Optional<Sale> findBySaleNumber(String saleNumber);

    List<Sale> findByGymId(UUID gymId);

    List<Sale> findByGymIdAndStatus(UUID gymId, SaleStatus status);

    List<Sale> findByMemberId(UUID memberId);

    List<Sale> findByStaffId(UUID staffId);

    List<Sale> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

    long countByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

    List<Sale> findTodaysSalesByGymId(UUID gymId, LocalDateTime today);

    long countCompletedByGymId(UUID gymId);

    BigDecimal sumTotalByGymId(UUID gymId);

    BigDecimal sumTotalByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

    BigDecimal sumTodaysTotalByGymId(UUID gymId);

    List<Object[]> sumTotalByPaymentTypeAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);
}
