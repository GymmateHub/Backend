package com.gymmate.pos.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Public read surface for the pos module — replaces the direct
 * {@code pos.internal.repository.SaleJpaRepository} import that {@code analytics} used
 * before this module's {@code .api}/{@code .internal} split (see the Phase 0 ADR,
 * decision 3 — this retires that Tier-1 debt for `pos`).
 */
public interface PosFacade {

    /** Sum of completed sale totals for a gym within a date range. */
    BigDecimal sumRevenueByGymIdAndDateRange(UUID gymId, LocalDateTime start, LocalDateTime end);

    /** Count of sales for a gym within a date range (any status). */
    long countTransactionsByGymIdAndDateRange(UUID gymId, LocalDateTime start, LocalDateTime end);
}
