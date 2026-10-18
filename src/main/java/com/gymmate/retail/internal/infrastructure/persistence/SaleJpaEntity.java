package com.gymmate.retail.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.GymScopedJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import com.gymmate.retail.internal.domain.Sale;
import com.gymmate.retail.internal.domain.PaymentType;
import com.gymmate.retail.internal.domain.SaleStatus;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link Sale} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "Sale")
@Table(name = "pos_sales")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(Sale.class)
public class SaleJpaEntity extends GymScopedJpaEntity {

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)
    @Column(name = "sale_number", nullable = false, unique = true, length = 50)
    private String saleNumber;

    @Column(name = "member_id")
    private UUID memberId; // Optional - can be a walk-in customer

    @Column(name = "customer_name", length = 200)
    private String customerName; // For walk-in customers

    @Column(name = "staff_id")
    private UUID staffId; // The staff member processing the sale

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private SaleStatus status = SaleStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 30)
    private PaymentType paymentType = PaymentType.CASH;

    // Amounts
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "discount_code", length = 50)
    private String discountCode;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "amount_paid", precision = 12, scale = 2)
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "change_given", precision = 12, scale = 2)
    private BigDecimal changeGiven = BigDecimal.ZERO;

    @Column(name = "refunded_amount", precision = 12, scale = 2)
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    // Payment reference
    @Column(name = "stripe_payment_intent_id", length = 100)
    private String stripePaymentIntentId;

    @Column(name = "external_reference", length = 100)
    private String externalReference;

    // Timestamps
    @Column(name = "sale_date", nullable = false)
    private LocalDateTime saleDate = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    // Additional info
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "receipt_printed")
    private boolean receiptPrinted = false;

    @Column(name = "receipt_emailed")
    private boolean receiptEmailed = false;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<SaleItemJpaEntity> items = new ArrayList<>();
}
