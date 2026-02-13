package com.gymmate.pos.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CashDrawer entity for tracking cash register sessions.
 * Extends GymScopedEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "pos_cash_drawers")
public class CashDrawer extends GymScopedEntity {

    @Column(name = "session_date", nullable = false)
    private LocalDate sessionDate;

    @Column(name = "opened_by", nullable = false)
    private UUID openedBy;

    @Column(name = "closed_by")
    private UUID closedBy;

    @Column(name = "opening_balance", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal openingBalance = BigDecimal.ZERO;

    @Column(name = "closing_balance", precision = 12, scale = 2)
    private BigDecimal closingBalance;

    @Column(name = "expected_balance", precision = 12, scale = 2)
    private BigDecimal expectedBalance;

    @Column(name = "variance", precision = 12, scale = 2)
    private BigDecimal variance;

    // Transaction totals
    @Column(name = "total_cash_sales", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalCashSales = BigDecimal.ZERO;

    @Column(name = "total_card_sales", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalCardSales = BigDecimal.ZERO;

    @Column(name = "total_other_sales", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalOtherSales = BigDecimal.ZERO;

    @Column(name = "total_refunds", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalRefunds = BigDecimal.ZERO;

    @Column(name = "transaction_count")
    @Builder.Default
    private Integer transactionCount = 0;

    // Timestamps
    @Column(name = "opened_at", nullable = false)
    @Builder.Default
    private LocalDateTime openedAt = LocalDateTime.now();

    @Column(name = "closed_at")
    private LocalDateTime closedAt;

    // Status
    @Column(name = "is_open", nullable = false)
    @Builder.Default
    private boolean open = true;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "closing_notes", columnDefinition = "TEXT")
    private String closingNotes;

    // Business methods
    public void addCashSale(BigDecimal amount) {
        this.totalCashSales = totalCashSales.add(amount);
        this.transactionCount++;
    }

    public void addCardSale(BigDecimal amount) {
        this.totalCardSales = totalCardSales.add(amount);
        this.transactionCount++;
    }

    public void addOtherSale(BigDecimal amount) {
        this.totalOtherSales = totalOtherSales.add(amount);
        this.transactionCount++;
    }

    public void addRefund(BigDecimal amount) {
        this.totalRefunds = totalRefunds.add(amount);
    }

    public BigDecimal getTotalSales() {
        return totalCashSales.add(totalCardSales).add(totalOtherSales);
    }

    public void close(UUID closedBy, BigDecimal closingBalance, String closingNotes) {
        this.closedBy = closedBy;
        this.closingBalance = closingBalance;
        this.expectedBalance = openingBalance.add(totalCashSales).subtract(totalRefunds);
        this.variance = closingBalance.subtract(expectedBalance);
        this.closedAt = LocalDateTime.now();
        this.open = false;
        this.closingNotes = closingNotes;
    }

    public boolean hasVariance() {
        return variance != null && variance.compareTo(BigDecimal.ZERO) != 0;
    }
}
