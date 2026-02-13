package com.gymmate.pos.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Sale entity representing a POS transaction.
 * Extends GymScopedEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "pos_sales")
public class Sale extends GymScopedEntity {

    // Note: gymId is inherited from GymScopedEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedEntity)

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
    @Builder.Default
    private SaleStatus status = SaleStatus.PENDING;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_type", nullable = false, length = 30)
    @Builder.Default
    private PaymentType paymentType = PaymentType.CASH;

    // Amounts
    @Column(name = "subtotal", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Column(name = "discount_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    @Column(name = "discount_percentage", precision = 5, scale = 2)
    private BigDecimal discountPercentage;

    @Column(name = "discount_code", length = 50)
    private String discountCode;

    @Column(name = "tax_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    @Column(name = "tax_rate", precision = 5, scale = 2)
    private BigDecimal taxRate;

    @Column(name = "total_amount", nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Column(name = "amount_paid", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Column(name = "change_given", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal changeGiven = BigDecimal.ZERO;

    @Column(name = "refunded_amount", precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    // Payment reference
    @Column(name = "stripe_payment_intent_id", length = 100)
    private String stripePaymentIntentId;

    @Column(name = "external_reference", length = 100)
    private String externalReference;

    // Timestamps
    @Column(name = "sale_date", nullable = false)
    @Builder.Default
    private LocalDateTime saleDate = LocalDateTime.now();

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    @Column(name = "refunded_at")
    private LocalDateTime refundedAt;

    // Additional info
    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "receipt_printed")
    @Builder.Default
    private boolean receiptPrinted = false;

    @Column(name = "receipt_emailed")
    @Builder.Default
    private boolean receiptEmailed = false;

    @OneToMany(mappedBy = "sale", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<SaleItem> items = new ArrayList<>();

    // Business methods
    public void addItem(SaleItem item) {
        items.add(item);
        item.setSale(this);
        recalculateTotals();
    }

    public void removeItem(SaleItem item) {
        items.remove(item);
        item.setSale(null);
        recalculateTotals();
    }

    public void recalculateTotals() {
        this.subtotal = items.stream()
                .map(SaleItem::getLineTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal discountedSubtotal = subtotal;
        if (discountPercentage != null && discountPercentage.compareTo(BigDecimal.ZERO) > 0) {
            this.discountAmount = subtotal.multiply(discountPercentage).divide(BigDecimal.valueOf(100));
            discountedSubtotal = subtotal.subtract(discountAmount);
        }

        if (taxRate != null && taxRate.compareTo(BigDecimal.ZERO) > 0) {
            this.taxAmount = discountedSubtotal.multiply(taxRate).divide(BigDecimal.valueOf(100));
        }

        this.totalAmount = discountedSubtotal.add(taxAmount != null ? taxAmount : BigDecimal.ZERO);
    }

    public void complete(PaymentType paymentType, BigDecimal amountPaid) {
        this.paymentType = paymentType;
        this.amountPaid = amountPaid;
        this.status = SaleStatus.COMPLETED;
        this.completedAt = LocalDateTime.now();

        if (paymentType == PaymentType.CASH && amountPaid.compareTo(totalAmount) > 0) {
            this.changeGiven = amountPaid.subtract(totalAmount);
        }
    }

    public void cancel() {
        this.status = SaleStatus.CANCELLED;
    }

    public void refund(BigDecimal amount) {
        this.refundedAmount = (refundedAmount != null ? refundedAmount : BigDecimal.ZERO).add(amount);
        this.refundedAt = LocalDateTime.now();

        if (refundedAmount.compareTo(totalAmount) >= 0) {
            this.status = SaleStatus.REFUNDED;
        } else {
            this.status = SaleStatus.PARTIALLY_REFUNDED;
        }
    }

    public boolean isPaid() {
        return status == SaleStatus.COMPLETED;
    }

    public BigDecimal getBalanceDue() {
        return totalAmount.subtract(amountPaid != null ? amountPaid : BigDecimal.ZERO);
    }

    public int getTotalItemCount() {
        return items.stream()
                .mapToInt(SaleItem::getQuantity)
                .sum();
    }
}
