package com.gymmate.pos.api.dto;

import com.gymmate.pos.domain.PaymentType;
import com.gymmate.pos.domain.Sale;
import com.gymmate.pos.domain.SaleStatus;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Response DTO for Sale entity.
 */
public record SaleResponse(
        UUID id,
        String saleNumber,
        UUID gymId,
        UUID memberId,
        String customerName,
        UUID staffId,
        SaleStatus status,
        PaymentType paymentType,
        BigDecimal subtotal,
        BigDecimal discountAmount,
        BigDecimal discountPercentage,
        String discountCode,
        BigDecimal taxAmount,
        BigDecimal taxRate,
        BigDecimal totalAmount,
        BigDecimal amountPaid,
        BigDecimal changeGiven,
        BigDecimal refundedAmount,
        String stripePaymentIntentId,
        LocalDateTime saleDate,
        LocalDateTime completedAt,
        String notes,
        boolean receiptPrinted,
        boolean receiptEmailed,
        int totalItemCount,
        List<SaleItemResponse> items,
        LocalDateTime createdAt,
        boolean active) {
    public static SaleResponse fromEntity(Sale sale) {
        return new SaleResponse(
                sale.getId(),
                sale.getSaleNumber(),
                sale.getGymId(),
                sale.getMemberId(),
                sale.getCustomerName(),
                sale.getStaffId(),
                sale.getStatus(),
                sale.getPaymentType(),
                sale.getSubtotal(),
                sale.getDiscountAmount(),
                sale.getDiscountPercentage(),
                sale.getDiscountCode(),
                sale.getTaxAmount(),
                sale.getTaxRate(),
                sale.getTotalAmount(),
                sale.getAmountPaid(),
                sale.getChangeGiven(),
                sale.getRefundedAmount(),
                sale.getStripePaymentIntentId(),
                sale.getSaleDate(),
                sale.getCompletedAt(),
                sale.getNotes(),
                sale.isReceiptPrinted(),
                sale.isReceiptEmailed(),
                sale.getTotalItemCount(),
                sale.getItems() != null
                        ? sale.getItems().stream().map(SaleItemResponse::fromEntity).collect(Collectors.toList())
                        : List.of(),
                sale.getCreatedAt(),
                sale.isActive());
    }
}
