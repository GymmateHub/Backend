package com.gymmate.unit.pos.domain;

import com.gymmate.pos.domain.PaymentType;
import com.gymmate.pos.domain.Sale;
import com.gymmate.pos.domain.SaleItem;
import com.gymmate.pos.domain.SaleStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Sale Domain Tests")
class SaleDomainTest {

    private Sale sale;
    private UUID gymId;
    private UUID memberId;
    private UUID staffId;

    @BeforeEach
    void setUp() {
        gymId = UUID.randomUUID();
        memberId = UUID.randomUUID();
        staffId = UUID.randomUUID();

        sale = Sale.builder()
                .saleNumber("SALE-001")
                .memberId(memberId)
                .customerName("John Doe")
                .staffId(staffId)
                .paymentType(PaymentType.CASH)
                .taxRate(BigDecimal.valueOf(10))
                .status(SaleStatus.PENDING)
                .saleDate(LocalDateTime.now())
                .build();
        sale.setGymId(gymId);
    }

    @Nested
    @DisplayName("Sale Item Management")
    class SaleItemTests {

        @Test
        @DisplayName("Should add item to sale")
        void addItem_Success() {
            // Arrange
            SaleItem item = createSaleItem("Protein Shake", BigDecimal.valueOf(25), 2);

            // Act
            sale.addItem(item);

            // Assert
            assertThat(sale.getItems()).hasSize(1);
            assertThat(sale.getItems().get(0).getItemName()).isEqualTo("Protein Shake");
        }

        @Test
        @DisplayName("Should remove item from sale")
        void removeItem_Success() {
            // Arrange
            SaleItem item = createSaleItem("Protein Shake", BigDecimal.valueOf(25), 2);
            sale.addItem(item);

            // Act
            sale.removeItem(item);

            // Assert
            assertThat(sale.getItems()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Total Calculations")
    class TotalCalculationTests {

        @Test
        @DisplayName("Should calculate subtotal correctly")
        void calculateSubtotal_MultipleItems_CorrectSum() {
            // Arrange
            sale.addItem(createSaleItem("Item 1", BigDecimal.valueOf(10), 2)); // 20
            sale.addItem(createSaleItem("Item 2", BigDecimal.valueOf(15), 1)); // 15

            // Act
            sale.recalculateTotals();

            // Assert
            assertThat(sale.getSubtotal()).isEqualByComparingTo(BigDecimal.valueOf(35));
        }

        @Test
        @DisplayName("Should apply discount correctly")
        void applyDiscount_CalculatesCorrectTotal() {
            // Arrange
            sale.setDiscountPercentage(BigDecimal.valueOf(20)); // 20% discount
            sale.addItem(createSaleItem("Item", BigDecimal.valueOf(100), 1)); // 100

            // Act
            sale.recalculateTotals();

            // Assert - 100 - 20% = 80
            assertThat(sale.getDiscountAmount()).isEqualByComparingTo(BigDecimal.valueOf(20));
        }

        @Test
        @DisplayName("Should calculate tax correctly")
        void calculateTax_CorrectAmount() {
            // Arrange
            sale.setTaxRate(BigDecimal.valueOf(10)); // 10% tax
            sale.addItem(createSaleItem("Item", BigDecimal.valueOf(100), 1));

            // Act
            sale.recalculateTotals();

            // Assert - Subtotal 100, after 0% discount, 10% tax = 10
            assertThat(sale.getTaxAmount()).isEqualByComparingTo(BigDecimal.valueOf(10));
        }

        @Test
        @DisplayName("Should calculate grand total with discount and tax")
        void calculateGrandTotal_WithDiscountAndTax() {
            // Arrange
            sale.setDiscountPercentage(BigDecimal.valueOf(10)); // 10% discount
            sale.setTaxRate(BigDecimal.valueOf(8)); // 8% tax
            sale.addItem(createSaleItem("Item", BigDecimal.valueOf(100), 1));

            // Act
            sale.recalculateTotals();

            // Assert
            // Subtotal: 100
            // Discount: 10 (10%)
            // After discount: 90
            // Tax: 7.20 (8%)
            // Total: 97.20
            assertThat(sale.getTotalAmount()).isEqualByComparingTo(BigDecimal.valueOf(97.20));
        }

        @Test
        @DisplayName("Should handle zero items correctly")
        void calculateTotals_NoItems_ZeroTotal() {
            // Act
            sale.recalculateTotals();

            // Assert
            assertThat(sale.getSubtotal()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(sale.getTotalAmount()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Sale Lifecycle")
    class SaleLifecycleTests {

        @Test
        @DisplayName("Should complete sale with correct amount")
        void complete_WithCorrectAmount_Success() {
            // Arrange
            sale.addItem(createSaleItem("Item", BigDecimal.valueOf(50), 2));
            sale.recalculateTotals();
            BigDecimal totalDue = sale.getTotalAmount();

            // Act
            sale.complete(PaymentType.CASH, totalDue);

            // Assert
            assertThat(sale.getStatus()).isEqualTo(SaleStatus.COMPLETED);
            assertThat(sale.getAmountPaid()).isEqualByComparingTo(totalDue);
            assertThat(sale.getChangeGiven()).isEqualByComparingTo(BigDecimal.ZERO);
        }

        @Test
        @DisplayName("Should calculate change when overpaid")
        void complete_WithOverpayment_CalculatesChange() {
            // Arrange
            sale.addItem(createSaleItem("Item", BigDecimal.valueOf(50), 1));
            sale.recalculateTotals();

            // Act - Pay with $100 bill
            sale.complete(PaymentType.CASH, BigDecimal.valueOf(100));

            // Assert
            assertThat(sale.getChangeGiven().doubleValue()).isGreaterThan(0);
        }

        @Test
        @DisplayName("Should cancel pending sale")
        void cancel_PendingSale_Success() {
            // Arrange
            assertThat(sale.getStatus()).isEqualTo(SaleStatus.PENDING);

            // Act
            sale.cancel();

            // Assert
            assertThat(sale.getStatus()).isEqualTo(SaleStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should process full refund")
        void refund_FullAmount_Success() {
            // Arrange
            sale.addItem(createSaleItem("Item", BigDecimal.valueOf(100), 1));
            sale.recalculateTotals();
            sale.complete(PaymentType.CASH, sale.getTotalAmount());

            // Act
            sale.refund(sale.getTotalAmount());

            // Assert
            assertThat(sale.getStatus()).isEqualTo(SaleStatus.REFUNDED);
            assertThat(sale.getRefundedAmount()).isEqualByComparingTo(sale.getTotalAmount());
        }

        @Test
        @DisplayName("Should process partial refund")
        void refund_PartialAmount_Success() {
            // Arrange
            sale.addItem(createSaleItem("Item", BigDecimal.valueOf(100), 1));
            sale.recalculateTotals();
            sale.complete(PaymentType.CASH, sale.getTotalAmount());

            // Act
            sale.refund(BigDecimal.valueOf(50));

            // Assert
            assertThat(sale.getStatus()).isEqualTo(SaleStatus.PARTIALLY_REFUNDED);
            assertThat(sale.getRefundedAmount()).isEqualByComparingTo(BigDecimal.valueOf(50));
        }
    }

    @Nested
    @DisplayName("Sale Status Checks")
    class StatusCheckTests {

        @Test
        @DisplayName("isPaid should return true for completed sales")
        void isPaid_CompletedSale_ReturnsTrue() {
            sale.addItem(createSaleItem("Item", BigDecimal.TEN, 1));
            sale.recalculateTotals();
            sale.complete(PaymentType.CASH, sale.getTotalAmount());

            assertThat(sale.isPaid()).isTrue();
        }

        @Test
        @DisplayName("getTotalItemCount should sum all quantities")
        void getTotalItemCount_MultipleItems_SumsQuantities() {
            sale.addItem(createSaleItem("Item1", BigDecimal.TEN, 2));
            sale.addItem(createSaleItem("Item2", BigDecimal.TEN, 3));

            assertThat(sale.getTotalItemCount()).isEqualTo(5);
        }

        @Test
        @DisplayName("getBalanceDue should show remaining balance")
        void getBalanceDue_PartialPayment_ShowsBalance() {
            sale.addItem(createSaleItem("Item", BigDecimal.valueOf(100), 1));
            sale.recalculateTotals();

            assertThat(sale.getBalanceDue()).isEqualByComparingTo(sale.getTotalAmount());
        }
    }

    // Helper method
    private SaleItem createSaleItem(String name, BigDecimal unitPrice, int quantity) {
        SaleItem item = SaleItem.builder()
                .inventoryItemId(UUID.randomUUID())
                .itemName(name)
                .itemSku("SKU-" + System.currentTimeMillis())
                .unitPrice(unitPrice)
                .quantity(quantity)
                .build();
        item.calculateLineTotal();
        return item;
    }
}
