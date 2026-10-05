package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.retail.internal.domain.MovementType;
import com.gymmate.retail.internal.domain.StockMovement;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link StockMovement} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "StockMovement")
@Table(name = "stock_movements")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(StockMovement.class)
public class StockMovementJpaEntity extends GymScopedJpaEntity {

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
    @Column(name = "inventory_item_id", nullable = false)
    private UUID inventoryItemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "movement_type", nullable = false, length = 50)
    private MovementType movementType;

    @Column(nullable = false)
    private Integer quantity;

    @Column(name = "unit_cost", precision = 10, scale = 2)
    private BigDecimal unitCost;

    @Column(name = "total_cost", precision = 10, scale = 2)
    private BigDecimal totalCost;

    @Column(name = "stock_before", nullable = false)
    private Integer stockBefore;

    @Column(name = "stock_after", nullable = false)
    private Integer stockAfter;

    @Column(name = "movement_date", nullable = false)
    private LocalDateTime movementDate = LocalDateTime.now();

    @Column(name = "reference_number", length = 100)
    private String // Invoice, PO number, etc.
    referenceNumber;

    @Column(name = "supplier_id")
    private UUID // For purchases
    supplierId;

    @Column(name = "customer_id")
    private UUID // For sales (member)
    customerId;

    @Column(name = "from_gym_id")
    private UUID // For transfers
    fromGymId;

    @Column(name = "to_gym_id")
    private UUID // For transfers
    toGymId;

    @Column(name = "batch_number", length = 100)
    private String batchNumber;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "performed_by")
    private String // User who made the movement
    performedBy;
}
