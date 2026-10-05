package com.gymmate.retail.internal.application;

import com.gymmate.retail.api.PosFacade;
import com.gymmate.retail.internal.application.port.SaleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class PosFacadeImpl implements PosFacade {

    private final SaleRepository saleJpaRepository;

    @Override
    public BigDecimal sumRevenueByGymIdAndDateRange(UUID gymId, LocalDateTime start, LocalDateTime end) {
        BigDecimal sum = saleJpaRepository.sumTotalByGymIdAndDateRange(gymId, start, end);
        return sum != null ? sum : BigDecimal.ZERO;
    }

    @Override
    public long countTransactionsByGymIdAndDateRange(UUID gymId, LocalDateTime start, LocalDateTime end) {
        return saleJpaRepository.countByGymIdAndDateRange(gymId, start, end);
    }
}
