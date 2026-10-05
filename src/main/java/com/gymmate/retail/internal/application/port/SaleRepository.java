package com.gymmate.retail.internal.application.port;

import com.gymmate.retail.internal.domain.Sale;
import com.gymmate.retail.internal.domain.SaleStatus;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/** Repository port for {@link Sale} aggregates. */
public interface SaleRepository {

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

    Sale save(Sale entity);

    List<Sale> saveAll(Iterable<Sale> entities);

    Optional<Sale> findById(UUID id);

    boolean existsById(UUID id);

    List<Sale> findAll();

    List<Sale> findAllById(Iterable<UUID> ids);

    long count();

    void deleteById(UUID id);

    void delete(Sale entity);

    void deleteAll(Iterable<Sale> entities);

    Sale saveAndFlush(Sale entity);

    void flush();

    Page<Sale> findAll(Pageable pageable);
}
