package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.retail.internal.domain.CashDrawer;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link CashDrawer} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "CashDrawer")
@Table(name = "pos_cash_drawers")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(CashDrawer.class)
public class CashDrawerJpaEntity extends GymScopedJpaEntity {

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "opened_by", nullable = false)
    private UUID openedBy;

    @Column(name = "closed_by")
    private UUID closedBy;

    @Column(name = "opening_balance", nullable = false, precision = 12, scale = 2)
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "closing_balance", precision = 12, scale = 2)
    private BigDecimal closingBalance;

    @Column(name = "expected_balance", precision = 12, scale = 2)
    private BigDecimal expectedBalance;

    @Column(name = "variance", precision = 12, scale = 2)
    private BigDecimal variance;

    // Transaction totals
    @Column(name = "total_cash_sales", precision = 12, scale = 2)
    private BigDecimal totalCashSales = BigDecimal.ZERO;

    @Column(name = "total_card_sales", precision = 12, scale = 2)
    private BigDecimal totalCardSales = BigDecimal.ZERO;

    @Column(name = "total_other_sales", precision = 12, scale = 2)
    private BigDecimal totalOtherSales = BigDecimal.ZERO;

    @Column(name = "total_refunds", precision = 12, scale = 2)
    private BigDecimal totalRefunds = BigDecimal.ZERO;

    @Column(name = "transaction_count")
    private Integer transactionCount = 0;

    // Timestamps
    @Column(name = "opened_at", nullable = false)
    private LocalDateTime openedAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // Status
    @Column(name = "is_open", nullable = false)
    private boolean open = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "closing_notes", columnDefinition = "TEXT")
    private String closingNotes;
}
