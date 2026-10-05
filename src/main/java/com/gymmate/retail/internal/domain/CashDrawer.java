package com.gymmate.retail.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CashDrawer entity for tracking cash register sessions.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class CashDrawer extends GymScopedEntity {

    private LocalDate sessionDate;

    private UUID openedBy;

    private UUID closedBy;

    @Builder.Default
    private 
    BigDecimal openingBalance = BigDecimal.ZERO;

    private BigDecimal closingBalance;

    private BigDecimal expectedBalance;

    private BigDecimal variance;

    // Transaction totals
    @Builder.Default
    private 
    BigDecimal totalCashSales = BigDecimal.ZERO;

    @Builder.Default
    private 
    BigDecimal totalCardSales = BigDecimal.ZERO;

    @Builder.Default
    private 
    BigDecimal totalOtherSales = BigDecimal.ZERO;

    @Builder.Default
    private 
    BigDecimal totalRefunds = BigDecimal.ZERO;

    @Builder.Default
    private 
    Integer transactionCount = 0;

    // Timestamps
    @Builder.Default
    private 
    LocalDateTime openedAt = LocalDateTime.now();

    private LocalDateTime closedAt;

    // Status
    @Builder.Default
    private 
    boolean open = true;

    private String notes;

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
