package com.gymmate.unit.pos.domain;

import com.gymmate.pos.domain.CashDrawer;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("CashDrawer Domain Tests")
class CashDrawerDomainTest {

    private CashDrawer cashDrawer;
    private UUID gymId;
    private UUID staffId;

    @BeforeEach
    void setUp() {
        gymId = UUID.randomUUID();
        staffId = UUID.randomUUID();

        cashDrawer = CashDrawer.builder()
                .sessionDate(LocalDate.now())
                .openedBy(staffId)
                .openingBalance(BigDecimal.valueOf(200))
                .notes("Opening shift")
                .open(true)
                .openedAt(LocalDateTime.now())
                .build();
        cashDrawer.setGymId(gymId);
    }

    @Nested
    @DisplayName("Cash Drawer State")
    class CashDrawerStateTests {

        @Test
        @DisplayName("Should be open initially")
        void initialState_IsOpen() {
            assertThat(cashDrawer.isOpen()).isTrue();
        }

        @Test
        @DisplayName("Should have correct opening balance")
        void initialState_HasOpeningBalance() {
            assertThat(cashDrawer.getOpeningBalance()).isEqualByComparingTo(BigDecimal.valueOf(200));
        }

        @Test
        @DisplayName("Should track who opened the drawer")
        void initialState_TracksOpenedBy() {
            assertThat(cashDrawer.getOpenedBy()).isEqualTo(staffId);
        }

        @Test
        @DisplayName("Should have zero transaction count initially")
        void initialState_ZeroTransactions() {
            assertThat(cashDrawer.getTransactionCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Close Cash Drawer")
    class CloseCashDrawerTests {

        @Test
        @DisplayName("Should close drawer with closing balance")
        void close_WithBalance_Success() {
            // Arrange
            UUID closingStaffId = UUID.randomUUID();
            BigDecimal closingBalance = BigDecimal.valueOf(350);

            // Act
            cashDrawer.close(closingStaffId, closingBalance, "End of shift");

            // Assert
            assertThat(cashDrawer.isOpen()).isFalse();
            assertThat(cashDrawer.getClosedBy()).isEqualTo(closingStaffId);
            assertThat(cashDrawer.getClosingBalance()).isEqualByComparingTo(closingBalance);
            assertThat(cashDrawer.getClosedAt()).isNotNull();
            assertThat(cashDrawer.getClosingNotes()).isEqualTo("End of shift");
        }

        @Test
        @DisplayName("Should calculate positive variance when more cash than expected")
        void close_MoreCashThanExpected_PositiveVariance() {
            // Arrange - Started with 200, added 100 in cash sales
            cashDrawer.addCashSale(BigDecimal.valueOf(100));
            // Expected balance = 200 + 100 = 300

            // Act - Actually have 350 (overage of 50)
            cashDrawer.close(UUID.randomUUID(), BigDecimal.valueOf(350), null);

            // Assert
            assertThat(cashDrawer.getExpectedBalance()).isEqualByComparingTo(BigDecimal.valueOf(300));
            assertThat(cashDrawer.getVariance()).isEqualByComparingTo(BigDecimal.valueOf(50));
            assertThat(cashDrawer.hasVariance()).isTrue();
        }

        @Test
        @DisplayName("Should calculate negative variance when less cash than expected")
        void close_LessCashThanExpected_NegativeVariance() {
            // Arrange - Expected 300 (200 + 100 in sales)
            cashDrawer.addCashSale(BigDecimal.valueOf(100));

            // Act - Actually have 280 (shortage of 20)
            cashDrawer.close(UUID.randomUUID(), BigDecimal.valueOf(280), null);

            // Assert
            assertThat(cashDrawer.getVariance()).isEqualByComparingTo(BigDecimal.valueOf(-20));
            assertThat(cashDrawer.hasVariance()).isTrue();
        }

        @Test
        @DisplayName("Should handle zero variance")
        void close_ExactBalance_NoVariance() {
            // Arrange
            cashDrawer.addCashSale(BigDecimal.valueOf(100));
            // Expected balance = 200 + 100 = 300

            // Act - Close with exact expected amount
            cashDrawer.close(UUID.randomUUID(), BigDecimal.valueOf(300), null);

            // Assert
            assertThat(cashDrawer.getVariance()).isEqualByComparingTo(BigDecimal.ZERO);
            assertThat(cashDrawer.hasVariance()).isFalse();
        }
    }

    @Nested
    @DisplayName("Cash Sale Tracking")
    class CashSaleTrackingTests {

        @Test
        @DisplayName("Should add cash sale and increment count")
        void addCashSale_UpdatesTotals() {
            // Act
            cashDrawer.addCashSale(BigDecimal.valueOf(50));

            // Assert
            assertThat(cashDrawer.getTotalCashSales()).isEqualByComparingTo(BigDecimal.valueOf(50));
            assertThat(cashDrawer.getTransactionCount()).isEqualTo(1);
        }

        @Test
        @DisplayName("Should accumulate multiple cash sales")
        void addCashSale_Multiple_Accumulates() {
            // Act
            cashDrawer.addCashSale(BigDecimal.valueOf(25));
            cashDrawer.addCashSale(BigDecimal.valueOf(30));
            cashDrawer.addCashSale(BigDecimal.valueOf(15));

            // Assert
            assertThat(cashDrawer.getTotalCashSales()).isEqualByComparingTo(BigDecimal.valueOf(70));
            assertThat(cashDrawer.getTransactionCount()).isEqualTo(3);
        }
    }

    @Nested
    @DisplayName("Card Sale Tracking")
    class CardSaleTrackingTests {

        @Test
        @DisplayName("Should add card sale and increment count")
        void addCardSale_UpdatesTotals() {
            // Act
            cashDrawer.addCardSale(BigDecimal.valueOf(75));

            // Assert
            assertThat(cashDrawer.getTotalCardSales()).isEqualByComparingTo(BigDecimal.valueOf(75));
            assertThat(cashDrawer.getTransactionCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Other Sale Tracking")
    class OtherSaleTrackingTests {

        @Test
        @DisplayName("Should add other payment type and increment count")
        void addOtherSale_UpdatesTotals() {
            // Act
            cashDrawer.addOtherSale(BigDecimal.valueOf(100));

            // Assert
            assertThat(cashDrawer.getTotalOtherSales()).isEqualByComparingTo(BigDecimal.valueOf(100));
            assertThat(cashDrawer.getTransactionCount()).isEqualTo(1);
        }
    }

    @Nested
    @DisplayName("Refund Tracking")
    class RefundTrackingTests {

        @Test
        @DisplayName("Should add refund")
        void addRefund_UpdatesTotal() {
            // Act
            cashDrawer.addRefund(BigDecimal.valueOf(30));

            // Assert
            assertThat(cashDrawer.getTotalRefunds()).isEqualByComparingTo(BigDecimal.valueOf(30));
        }

        @Test
        @DisplayName("Refund should affect expected balance")
        void addRefund_AffectsExpectedBalance() {
            // Arrange
            cashDrawer.addCashSale(BigDecimal.valueOf(100));
            cashDrawer.addRefund(BigDecimal.valueOf(20));

            // Act
            cashDrawer.close(UUID.randomUUID(), BigDecimal.valueOf(280), null);

            // Assert - Expected: 200 (opening) + 100 (cash sales) - 20 (refunds) = 280
            assertThat(cashDrawer.getExpectedBalance()).isEqualByComparingTo(BigDecimal.valueOf(280));
            assertThat(cashDrawer.hasVariance()).isFalse();
        }
    }

    @Nested
    @DisplayName("Total Sales Calculation")
    class TotalSalesTests {

        @Test
        @DisplayName("Should sum all payment types")
        void getTotalSales_SumsAllTypes() {
            // Arrange
            cashDrawer.addCashSale(BigDecimal.valueOf(100));
            cashDrawer.addCardSale(BigDecimal.valueOf(75));
            cashDrawer.addOtherSale(BigDecimal.valueOf(25));

            // Act
            BigDecimal total = cashDrawer.getTotalSales();

            // Assert
            assertThat(total).isEqualByComparingTo(BigDecimal.valueOf(200));
        }

        @Test
        @DisplayName("Should return zero when no sales")
        void getTotalSales_NoSales_ReturnsZero() {
            assertThat(cashDrawer.getTotalSales()).isEqualByComparingTo(BigDecimal.ZERO);
        }
    }

    @Nested
    @DisplayName("Session Information")
    class SessionInfoTests {

        @Test
        @DisplayName("Should track session date")
        void hasSessionDate() {
            assertThat(cashDrawer.getSessionDate()).isEqualTo(LocalDate.now());
        }

        @Test
        @DisplayName("Should track opening time")
        void hasOpenedAtTime() {
            assertThat(cashDrawer.getOpenedAt()).isNotNull();
            assertThat(cashDrawer.getOpenedAt()).isBeforeOrEqualTo(LocalDateTime.now());
        }

        @Test
        @DisplayName("Should track notes")
        void hasNotes() {
            assertThat(cashDrawer.getNotes()).isEqualTo("Opening shift");
        }
    }
}
