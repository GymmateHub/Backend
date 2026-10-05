package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.util.UUID;
import com.gymmate.retail.internal.domain.InventoryItem;
import com.gymmate.retail.internal.domain.InventoryCategory;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link InventoryItem} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "InventoryItem")
@Table(name = "inventory_items")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(InventoryItem.class)
public class InventoryItemJpaEntity extends GymScopedJpaEntity {

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
    @Column(nullable = false, length = 200)
    private String name;

    @Column(length = 100, unique = true)
    private String // Stock Keeping Unit
    sku;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private InventoryCategory category = InventoryCategory.OTHER;

    @Column(columnDefinition = "TEXT")
    private String description;

    // Stock levels
    @Column(name = "current_stock", nullable = false)
    private Integer currentStock = 0;

    @Column(name = "minimum_stock")
    private Integer // Alert threshold
    minimumStock = 0;

    @Column(name = "maximum_stock")
    private Integer // Maximum capacity
    maximumStock;

    @Column(name = "reorder_point")
    private Integer // When to reorder
    reorderPoint = 0;

    @Column(name = "reorder_quantity")
    private Integer // How much to reorder
    reorderQuantity;

    // Pricing
    @Column(name = "unit_cost", precision = 10, scale = 2)
    private BigDecimal // Cost per unit from supplier
    unitCost;

    @Column(name = "unit_price", precision = 10, scale = 2)
    private BigDecimal // Selling price per unit
    unitPrice;

    @Column(length = 20)
    private String // piece, box, kg, liter, etc.
    unit;

    // Supplier information
    @Column(name = "supplier_id")
    private UUID supplierId;

    @Column(name = "supplier_product_code", length = 100)
    private String supplierProductCode;

    // Tracking
    @Column(name = "barcode", length = 100)
    private String barcode;

    @Column(name = "location", length = 200)
    private String // Storage location within gym
    location;

    @Column(name = "expiry_tracking")
    private boolean // For perishable items
    expiryTracking = false;

    @Column(name = "batch_tracking")
    private boolean batchTracking = false;

    // Additional info
    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "low_stock_alert_sent")
    private boolean lowStockAlertSent = false;
}
