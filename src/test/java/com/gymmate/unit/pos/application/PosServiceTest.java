package com.gymmate.unit.pos.application;

import com.gymmate.inventory.application.InventoryService;
import com.gymmate.pos.api.dto.CreateSaleRequest;
import com.gymmate.pos.api.dto.SaleItemRequest;
import com.gymmate.pos.application.PosService;
import com.gymmate.pos.domain.*;
import com.gymmate.pos.infrastructure.CashDrawerJpaRepository;
import com.gymmate.pos.infrastructure.SaleItemJpaRepository;
import com.gymmate.pos.infrastructure.SaleJpaRepository;
import com.gymmate.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("PosService Unit Tests")
class PosServiceTest {

    @Mock
    private SaleJpaRepository saleRepository;

    @Mock
    private SaleItemJpaRepository saleItemRepository;

    @Mock
    private CashDrawerJpaRepository cashDrawerRepository;

    @Mock
    private InventoryService inventoryService;

    private PosService posService;

    private UUID gymId;
    private UUID staffId;
    private UUID memberId;

    @BeforeEach
    void setUp() {
        posService = new PosService(
                saleRepository,
                saleItemRepository,
                cashDrawerRepository,
                inventoryService);

        gymId = UUID.randomUUID();
        staffId = UUID.randomUUID();
        memberId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Create Sale Tests")
    class CreateSaleTests {

        @Test
        @DisplayName("Should create sale with items")
        void createSale_WithItems_Success() {
            // Arrange
            UUID inventoryItemId = UUID.randomUUID();
            SaleItemRequest saleItem = new SaleItemRequest(
                    inventoryItemId,
                    "Protein Bar",
                    "SKU-001",
                    null, // itemBarcode
                    2, // quantity
                    BigDecimal.valueOf(5), // unitPrice
                    null, // costPrice
                    null, // discountPercentage
                    null // notes
            );

            CreateSaleRequest request = new CreateSaleRequest(
                    gymId,
                    memberId,
                    "John Doe",
                    staffId,
                    List.of(saleItem),
                    PaymentType.CASH,
                    BigDecimal.ZERO, // discountPercentage
                    null, // discountCode
                    BigDecimal.valueOf(8), // taxRate
                    null, // amountPaid
                    "Test sale" // notes
            );

            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> {
                Sale s = inv.getArgument(0);
                s.setId(UUID.randomUUID());
                return s;
            });

            // Act
            Sale result = posService.createSale(request, staffId);

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.getGymId()).isEqualTo(gymId);
            assertThat(result.getMemberId()).isEqualTo(memberId);
            assertThat(result.getStatus()).isEqualTo(SaleStatus.PENDING);
            assertThat(result.getItems()).hasSize(1);

            verify(saleRepository, times(1)).save(any(Sale.class)); // Initial + after items
        }

        @Test
        @DisplayName("Should generate unique sale number")
        void createSale_GeneratesUniqueSaleNumber() {
            // Arrange
            SaleItemRequest saleItem = new SaleItemRequest(
                    UUID.randomUUID(),
                    "Test Item",
                    "SKU-001",
                    null, 1, BigDecimal.TEN, null, null, null);

            CreateSaleRequest request = new CreateSaleRequest(
                    gymId, null, null, staffId,
                    List.of(saleItem),
                    PaymentType.CASH,
                    BigDecimal.ZERO, null, BigDecimal.ZERO, null, null);

            when(saleRepository.save(any(Sale.class))).thenAnswer(inv -> {
                Sale s = inv.getArgument(0);
                s.setId(UUID.randomUUID());
                return s;
            });

            // Act
            Sale result = posService.createSale(request, staffId);

            // Assert
            assertThat(result.getSaleNumber()).isNotNull();
            assertThat(result.getSaleNumber()).startsWith("POS_SALE-");
        }
    }

    @Nested
    @DisplayName("Complete Sale Tests")
    class CompleteSaleTests {

        @Test
        @DisplayName("Should complete sale")
        void completeSale_Success() {
            // Arrange
            UUID saleId = UUID.randomUUID();
            UUID inventoryItemId = UUID.randomUUID();
            Sale sale = createPendingSale(saleId, inventoryItemId);

            when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Sale result = posService.completeSale(saleId, PaymentType.CASH,
                    sale.getTotalAmount(), null);

            // Assert
            assertThat(result.getStatus()).isEqualTo(SaleStatus.COMPLETED);
            // Verify inventory service was called for sale recording
            verify(inventoryService).recordSale(eq(inventoryItemId), eq(2), any(), any(), any(), any());
        }

        @Test
        @DisplayName("Should handle card payment with Stripe")
        void completeSale_CardPayment_WithStripeId() {
            // Arrange
            UUID saleId = UUID.randomUUID();
            Sale sale = createPendingSale(saleId, UUID.randomUUID());
            String stripePaymentId = "pi_test_123";

            when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Sale result = posService.completeSale(saleId, PaymentType.CARD,
                    sale.getTotalAmount(), stripePaymentId);

            // Assert
            assertThat(result.getPaymentType()).isEqualTo(PaymentType.CARD);
            assertThat(result.getStripePaymentIntentId()).isEqualTo(stripePaymentId);
        }

        @Test
        @DisplayName("Should throw when sale not found")
        void completeSale_NotFound_ThrowsException() {
            // Arrange
            UUID saleId = UUID.randomUUID();
            when(saleRepository.findById(saleId)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> posService.completeSale(saleId, PaymentType.CASH,
                    BigDecimal.valueOf(100), null))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("Cancel Sale Tests")
    class CancelSaleTests {

        @Test
        @DisplayName("Should cancel pending sale")
        void cancelSale_PendingSale_Success() {
            // Arrange
            UUID saleId = UUID.randomUUID();
            Sale sale = createPendingSale(saleId, UUID.randomUUID());

            when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Sale result = posService.cancelSale(saleId);

            // Assert
            assertThat(result.getStatus()).isEqualTo(SaleStatus.CANCELLED);
        }
    }

    @Nested
    @DisplayName("Refund Sale Tests")
    class RefundSaleTests {

        @Test
        @DisplayName("Should process full refund")
        void refundSale_FullRefund_Success() {
            // Arrange
            UUID saleId = UUID.randomUUID();
            UUID inventoryItemId = UUID.randomUUID();
            Sale sale = createCompletedSale(saleId, inventoryItemId);
            BigDecimal refundAmount = sale.getTotalAmount();

            when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Sale result = posService.refundSale(saleId, refundAmount, "Customer returned");

            // Assert
            assertThat(result.getStatus()).isEqualTo(SaleStatus.REFUNDED);
        }

        @Test
        @DisplayName("Should process partial refund")
        void refundSale_PartialRefund_Success() {
            // Arrange
            UUID saleId = UUID.randomUUID();
            Sale sale = createCompletedSale(saleId, UUID.randomUUID());
            BigDecimal partialRefund = sale.getTotalAmount().divide(BigDecimal.valueOf(2));

            when(saleRepository.findById(saleId)).thenReturn(Optional.of(sale));
            when(saleRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            Sale result = posService.refundSale(saleId, partialRefund, "Partial refund");

            // Assert
            assertThat(result.getStatus()).isEqualTo(SaleStatus.PARTIALLY_REFUNDED);
            assertThat(result.getRefundedAmount()).isEqualByComparingTo(partialRefund);
        }
    }

    @Nested
    @DisplayName("Cash Drawer Tests")
    class CashDrawerTests {

        @Test
        @DisplayName("Should open new cash drawer")
        void openCashDrawer_Success() {
            // Arrange
            BigDecimal openingBalance = BigDecimal.valueOf(200);

            when(cashDrawerRepository.findOpenDrawerByGymId(gymId)).thenReturn(Optional.empty());
            when(cashDrawerRepository.save(any(CashDrawer.class))).thenAnswer(inv -> {
                CashDrawer d = inv.getArgument(0);
                d.setId(UUID.randomUUID());
                return d;
            });

            // Act
            CashDrawer result = posService.openCashDrawer(gymId, staffId, openingBalance, "Opening shift");

            // Assert
            assertThat(result).isNotNull();
            assertThat(result.isOpen()).isTrue();
            assertThat(result.getOpeningBalance()).isEqualByComparingTo(openingBalance);
            assertThat(result.getOpenedBy()).isEqualTo(staffId);
        }

        @Test
        @DisplayName("Should not open drawer when one already exists")
        void openCashDrawer_AlreadyOpen_ThrowsException() {
            // Arrange
            CashDrawer existingDrawer = CashDrawer.builder()
                    .open(true)
                    .openingBalance(BigDecimal.valueOf(100))
                    .build();

            when(cashDrawerRepository.findOpenDrawerByGymId(gymId))
                    .thenReturn(Optional.of(existingDrawer));

            // Act & Assert
            assertThatThrownBy(() -> posService.openCashDrawer(gymId, staffId,
                    BigDecimal.valueOf(200), null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("already an open cash drawer");
        }

        @Test
        @DisplayName("Should close cash drawer")
        void closeCashDrawer_Success() {
            // Arrange
            UUID drawerId = UUID.randomUUID();
            CashDrawer drawer = CashDrawer.builder()
                    .sessionDate(LocalDate.now())
                    .openedBy(staffId)
                    .openingBalance(BigDecimal.valueOf(200))
                    .open(true)
                    .openedAt(LocalDateTime.now())
                    .build();
            drawer.setId(drawerId);
            drawer.setGymId(gymId);

            when(cashDrawerRepository.findById(drawerId)).thenReturn(Optional.of(drawer));
            when(cashDrawerRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

            // Act
            CashDrawer result = posService.closeCashDrawer(drawerId, staffId,
                    BigDecimal.valueOf(350), "End of shift");

            // Assert
            assertThat(result.isOpen()).isFalse();
            assertThat(result.getClosingBalance()).isEqualByComparingTo(BigDecimal.valueOf(350));
        }
    }

    @Nested
    @DisplayName("Sales Query Tests")
    class SalesQueryTests {

        @Test
        @DisplayName("Should get sales by gym")
        void getSalesByGym_ReturnsList() {
            // Arrange
            Sale sale1 = createPendingSale(UUID.randomUUID(), UUID.randomUUID());
            Sale sale2 = createPendingSale(UUID.randomUUID(), UUID.randomUUID());

            when(saleRepository.findByGymId(gymId)).thenReturn(List.of(sale1, sale2));

            // Act
            List<Sale> result = posService.getSalesByGym(gymId);

            // Assert
            assertThat(result).hasSize(2);
        }

        @Test
        @DisplayName("Should get today's sales")
        void getTodaysSales_ReturnsList() {
            // Arrange
            when(saleRepository.findTodaysSalesByGymId(eq(gymId), any(LocalDateTime.class)))
                    .thenReturn(List.of());

            // Act
            List<Sale> result = posService.getTodaysSales(gymId);

            // Assert
            assertThat(result).isEmpty();
            verify(saleRepository).findTodaysSalesByGymId(eq(gymId), any(LocalDateTime.class));
        }
    }

    // Helper methods
    private Sale createPendingSale(UUID saleId, UUID inventoryItemId) {
        SaleItem item = SaleItem.builder()
                .inventoryItemId(inventoryItemId)
                .itemName("Test Item")
                .itemSku("SKU-001")
                .unitPrice(BigDecimal.valueOf(25))
                .quantity(2)
                .build();
        item.calculateLineTotal();

        Sale sale = Sale.builder()
                .saleNumber("SALE-TEST-001")
                .memberId(memberId)
                .staffId(staffId)
                .paymentType(PaymentType.CASH)
                .taxRate(BigDecimal.valueOf(8))
                .status(SaleStatus.PENDING)
                .saleDate(LocalDateTime.now())
                .build();
        sale.setId(saleId);
        sale.setGymId(gymId);
        sale.addItem(item);
        sale.recalculateTotals();

        return sale;
    }

    private Sale createCompletedSale(UUID saleId, UUID inventoryItemId) {
        Sale sale = createPendingSale(saleId, inventoryItemId);
        sale.complete(PaymentType.CASH, sale.getTotalAmount());
        return sale;
    }
}
