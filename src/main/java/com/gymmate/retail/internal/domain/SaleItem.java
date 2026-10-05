package com.gymmate.retail.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * SaleItem entity representing individual items in a POS sale.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class SaleItem extends GymScopedEntity {

    private Sale sale;

    private UUID inventoryItemId; // Reference to inventory item

    private String itemName;

    private String itemSku;

    private String itemBarcode;

    private Integer quantity;

    private BigDecimal unitPrice;

    private BigDecimal costPrice; // For profit tracking

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private BigDecimal discountPercentage;

    @Builder.Default
    private BigDecimal lineTotal = BigDecimal.ZERO;

    private String notes;

    @Builder.Default
    private boolean refunded = false;

    @Builder.Default
    private Integer refundedQuantity = 0;

    // Business methods
    public void calculateLineTotal() {
        LineTotals totals = lineTotals(unitPrice, quantity, discountPercentage, discountAmount);
        this.discountAmount = totals.discountAmount();
        this.lineTotal = totals.lineTotal();
    }

    /** Derived amounts of a sale line; applied on every write so they can never go stale. */
    public record LineTotals(BigDecimal discountAmount, BigDecimal lineTotal) {
    }

    public static LineTotals lineTotals(BigDecimal unitPrice, Integer quantity, BigDecimal discountPercentage,
                                        BigDecimal discountAmount) {
        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));

        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            discountAmount = gross.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
        }

        return new LineTotals(discountAmount, gross.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO));
    }

    public BigDecimal getProfit() {
        if (costPrice == null) {
            return BigDecimal.ZERO;
        }
        BigDecimal totalCost = costPrice.multiply(BigDecimal.valueOf(quantity));
        return lineTotal.subtract(totalCost);
    }

    public BigDecimal getProfitMargin() {
        if (lineTotal == null || lineTotal.compareTo(BigDecimal.ZERO) == 0) {
            return BigDecimal.ZERO;
        }
        return getProfit().divide(lineTotal, 4, java.math.RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));
    }

    public void refund(int quantity) {
        this.refundedQuantity += quantity;
        if (this.refundedQuantity >= this.quantity) {
            this.refunded = true;
        }
    }
}
