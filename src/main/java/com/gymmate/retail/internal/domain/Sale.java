package com.gymmate.retail.internal.domain;

import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Sale entity representing a POS transaction.
 * Extends GymScopedJpaEntity for automatic organisation and gym filtering.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class Sale extends GymScopedEntity {

    // Note: gymId is inherited from GymScopedJpaEntity
    // Note: organisationId is inherited from TenantEntity (via GymScopedJpaEntity)

    private String saleNumber;

    private UUID memberId; // Optional - can be a walk-in customer

    private String customerName; // For walk-in customers

    private UUID staffId; // The staff member processing the sale

    @Builder.Default
    private SaleStatus status = SaleStatus.PENDING;

    @Builder.Default
    private PaymentType paymentType = PaymentType.CASH;

    // Amounts
    @Builder.Default
    private BigDecimal subtotal = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal discountAmount = BigDecimal.ZERO;

    private BigDecimal discountPercentage;

    private String discountCode;

    @Builder.Default
    private BigDecimal taxAmount = BigDecimal.ZERO;

    private BigDecimal taxRate;

    @Builder.Default
    private BigDecimal totalAmount = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal amountPaid = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal changeGiven = BigDecimal.ZERO;

    @Builder.Default
    private BigDecimal refundedAmount = BigDecimal.ZERO;

    // Payment reference
    private String stripePaymentIntentId;

    private String externalReference;

    // Timestamps
    @Builder.Default
    private LocalDateTime saleDate = LocalDateTime.now();

    private LocalDateTime completedAt;

    private LocalDateTime refundedAt;

    // Additional info
    private String notes;

    @Builder.Default
    private boolean receiptPrinted = false;

    @Builder.Default
    private boolean receiptEmailed = false;

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
