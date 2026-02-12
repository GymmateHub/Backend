package com.gymmate.pos.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * SaleItem entity representing individual items in a POS sale.
 * Extends GymScopedEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "pos_sale_items")
public class SaleItem extends GymScopedEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private Sale sale;

    @Column(name = "inventory_item_id")
    private UUID inventoryItemId; // Reference to inventory item

    @Column(name = "item_name", nullable = false, length = 200)
    private String itemName;

    @Column(name = "item_sku", length = 100)
    private String itemSku;

    @Column(name = "item_barcode", length = 100)
    private String itemBarcode;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_price", nullable = false, precision = 12, scale = 2)
    private BigDecimal unitPrice;

    @Column(name = "cost_price", precision = 12, scale = 2)
    private BigDecimal costPrice; // For profit tracking

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal lineTotal = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "refunded")
    @Builder.Default
    private boolean refunded = false;

    @Column(name = "refunded_quantity")
    @Builder.Default
    private Integer refundedQuantity = 0;

    // Business methods
    public void calculateLineTotal() {
        BigDecimal gross = unitPrice.multiply(BigDecimal.valueOf(quantity));

        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = gross.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
        }

        this.lineTotal = gross.subtract(discountAmount != null ? discountAmount : BigDecimal.ZERO);
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

    @PrePersist
    @PreUpdate
    protected void calculateBeforeSave() {
        calculateLineTotal();
    }
}
