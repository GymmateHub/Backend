package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;
import com.gymmate.retail.internal.domain.SaleItem;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link SaleItem} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "SaleItem")
@Table(name = "pos_sale_items")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(SaleItem.class)
public class SaleItemJpaEntity extends GymScopedJpaEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sale_id", nullable = false)
    private SaleJpaEntity sale;

    @Column(name = "inventory_item_id")
    private UUID // Reference to inventory item
    inventoryItemId;

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
    private BigDecimal // For profit tracking
    costPrice;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "line_total", nullable = false, precision = 12, scale = 2)
    private BigDecimal lineTotal = BigDecimal.ZERO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "refunded")
    private boolean refunded = false;

    @Column(name = "refunded_quantity")
    private Integer refundedQuantity = 0;

    @PrePersist
    @PreUpdate
    protected void calculateBeforeSave() {
        // Derived amounts are recomputed on every write, with the domain's own rule.
        SaleItem.LineTotals totals = SaleItem.lineTotals(unitPrice, quantity, discountPercentage, discountAmount);
        this.discountAmount = totals.discountAmount();
        this.lineTotal = totals.lineTotal();
    }
}
