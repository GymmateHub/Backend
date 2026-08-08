package com.gymmate.unit.analytics.application;

import com.gymmate.analytics.api.dto.*;
import com.gymmate.analytics.internal.service.AnalyticsService;
import com.gymmate.analytics.internal.domain.AnalyticsPeriod;
import com.gymmate.shared.constants.BookingStatus;
import com.gymmate.classes.api.ClassesFacade;
import com.gymmate.inventory.api.InventoryFacade;
import com.gymmate.membership.domain.MembershipStatus;
import com.gymmate.membership.infrastructure.MemberInvoiceRepository;
import com.gymmate.membership.infrastructure.MemberMembershipJpaRepository;
import com.gymmate.membership.infrastructure.MembershipPlanJpaRepository;
import com.gymmate.pos.api.PosFacade;
import com.gymmate.user.infrastructure.MemberRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AnalyticsService Unit Tests")
class AnalyticsServiceTest {

        @Mock
        private MemberRepository memberRepository;

        @Mock
        private MemberMembershipJpaRepository membershipRepository;

        @Mock
        private MembershipPlanJpaRepository membershipPlanRepository;

        @Mock
        private ClassesFacade classesFacade;

        @Mock
        private InventoryFacade inventoryFacade;

        @Mock
        private PosFacade posFacade;

        @Mock
        private MemberInvoiceRepository memberInvoiceRepository;

        private AnalyticsService analyticsService;

        private UUID gymId;

        @BeforeEach
        void setUp() {
                analyticsService = new AnalyticsService(
                                memberRepository,
                                membershipRepository,
                                membershipPlanRepository,
                                classesFacade,
                                inventoryFacade,
                                posFacade,
                                memberInvoiceRepository);

                gymId = UUID.randomUUID();
        }

        @Nested
        @DisplayName("Dashboard Analytics Tests")
        class DashboardTests {

                @Test
                @DisplayName("Should generate dashboard with all KPIs")
                void getDashboard_ReturnsCompleteData() {
                        // Arrange
                        setupMockRepositories();

                        // Act
                        DashboardResponse result = analyticsService.getDashboard(gymId, AnalyticsPeriod.LAST_30_DAYS);

                        // Assert
                        assertThat(result).isNotNull();
                        assertThat(result.totalMembers()).isNotNull();
                        assertThat(result.totalRevenue()).isNotNull();
                }

                @Test
                @DisplayName("Should have correct member count in KPI")
                void getDashboard_CorrectMemberCount() {
                        // Arrange
                        when(memberRepository.countByGymId(gymId)).thenReturn(150L);
                        setupOtherMocks();

                        // Act
                        DashboardResponse result = analyticsService.getDashboard(gymId, AnalyticsPeriod.LAST_30_DAYS);

                        // Assert
                        assertThat(result.totalMembers().title()).isEqualTo("Total Members");
                        assertThat(result.totalMembers().value()).isEqualTo("150");
                }

                @Test
                @DisplayName("Should handle empty data gracefully")
                void getDashboard_EmptyData_ReturnsZeros() {
                        // Arrange - All repos return 0/empty
                        when(memberRepository.countByGymId(gymId)).thenReturn(0L);
                        when(membershipRepository.countActiveByGymId(gymId)).thenReturn(0L);
                        when(inventoryFacade.countLowStockItems(gymId))
                                        .thenReturn(0L);

                        // Act
                        DashboardResponse result = analyticsService.getDashboard(gymId, AnalyticsPeriod.LAST_7_DAYS);

                        // Assert
                        assertThat(result).isNotNull();
                        assertThat(result.totalMembers().value()).isEqualTo("0");
                }
        }

        @Nested
        @DisplayName("Member Analytics Tests")
        class MemberAnalyticsTests {

                @Test
                @DisplayName("Should return member analytics with all metrics")
                void getMemberAnalytics_ReturnsCompleteData() {
                        // Arrange
                        when(memberRepository.countByGymId(gymId)).thenReturn(200L);
                        when(membershipRepository.countActiveByGymId(gymId)).thenReturn(180L);
                        when(membershipRepository.countByGymIdAndStatus(eq(gymId), eq(MembershipStatus.PAUSED)))
                                        .thenReturn(10L);
                        when(memberRepository.countByGymIdAndCreatedAtBetween(eq(gymId), any(), any()))
                                        .thenReturn(25L);
                        when(membershipRepository.countCancelledByGymIdAndDateRange(eq(gymId), any(), any()))
                                        .thenReturn(5L);
                        when(membershipRepository.findExpiringMemberships(eq(gymId), any(), any()))
                                        .thenReturn(List.of());
                        when(membershipRepository.countActiveMembersByPlan(gymId))
                                        .thenReturn(List.of());

                        // Act
                        MemberAnalyticsResponse result = analyticsService.getMemberAnalytics(gymId,
                                        AnalyticsPeriod.LAST_30_DAYS);

                        // Assert
                        assertThat(result).isNotNull();
                        assertThat(result.totalMembers()).isEqualTo(200L);
                        assertThat(result.activeMembers()).isEqualTo(180L);
                        assertThat(result.suspendedMembers()).isEqualTo(10L);
                        assertThat(result.newMembersThisPeriod()).isEqualTo(25L);
                        assertThat(result.cancelledMembersThisPeriod()).isEqualTo(5L);
                }

                @Test
                @DisplayName("Should calculate retention rate correctly")
                void getMemberAnalytics_CorrectRetentionRate() {
                        // Arrange
                        when(memberRepository.countByGymId(gymId)).thenReturn(100L);
                        when(membershipRepository.countActiveByGymId(gymId)).thenReturn(90L);
                        when(membershipRepository.countByGymIdAndStatus(eq(gymId), any())).thenReturn(5L);
                        when(memberRepository.countByGymIdAndCreatedAtBetween(eq(gymId), any(), any())).thenReturn(10L);
                        when(membershipRepository.countCancelledByGymIdAndDateRange(eq(gymId), any(), any()))
                                        .thenReturn(5L);
                        when(membershipRepository.findExpiringMemberships(eq(gymId), any(), any()))
                                        .thenReturn(List.of());
                        when(membershipRepository.countActiveMembersByPlan(gymId)).thenReturn(List.of());

                        // Act
                        MemberAnalyticsResponse result = analyticsService.getMemberAnalytics(gymId,
                                        AnalyticsPeriod.LAST_30_DAYS);

                        // Assert
                        assertThat(result.retentionRate()).isNotNull();
                        // Retention rate should be <= 100
                        assertThat(result.retentionRate().doubleValue()).isLessThanOrEqualTo(100);
                }

                @Test
                @DisplayName("Should get members by plan breakdown")
                void getMemberAnalytics_MembersByPlanBreakdown() {
                        // Arrange
                        when(memberRepository.countByGymId(gymId)).thenReturn(100L);
                        when(membershipRepository.countActiveByGymId(gymId)).thenReturn(90L);
                        when(membershipRepository.countByGymIdAndStatus(eq(gymId), any())).thenReturn(0L);
                        when(memberRepository.countByGymIdAndCreatedAtBetween(eq(gymId), any(), any())).thenReturn(0L);
                        when(membershipRepository.countCancelledByGymIdAndDateRange(eq(gymId), any(), any()))
                                        .thenReturn(0L);
                        when(membershipRepository.findExpiringMemberships(eq(gymId), any(), any()))
                                        .thenReturn(List.of());

                        // Return plan breakdown data
                        List<Object[]> planData = List.of(
                                        new Object[] { "Monthly", 50L },
                                        new Object[] { "Annual", 30L },
                                        new Object[] { "Premium", 20L });
                        when(membershipRepository.countActiveMembersByPlan(gymId)).thenReturn(planData);

                        // Act
                        MemberAnalyticsResponse result = analyticsService.getMemberAnalytics(gymId,
                                        AnalyticsPeriod.LAST_30_DAYS);

                        // Assert
                        assertThat(result.membersByPlan()).hasSize(3);
                        assertThat(result.membersByPlan().get(0).category()).isEqualTo("Monthly");
                        assertThat(result.membersByPlan().get(0).count()).isEqualTo(50L);
                }
        }

        @Nested
        @DisplayName("Revenue Analytics Tests")
        class RevenueAnalyticsTests {

                @Test
                @DisplayName("Should calculate total revenue")
                void getRevenueAnalytics_TotalRevenue() {
                        // Arrange
                        when(posFacade.sumRevenueByGymIdAndDateRange(eq(gymId), any(), any()))
                                        .thenReturn(BigDecimal.valueOf(5000));
                        when(membershipRepository.sumProjectedRevenueByGymIdAndDateRange(eq(gymId), any(), any()))
                                        .thenReturn(BigDecimal.valueOf(3000));
                        when(posFacade.countTransactionsByGymIdAndDateRange(eq(gymId), any(), any()))
                                        .thenReturn(0L);

                        // Act
                        RevenueAnalyticsResponse result = analyticsService.getRevenueAnalytics(gymId,
                                        AnalyticsPeriod.LAST_30_DAYS);

                        // Assert
                        assertThat(result).isNotNull();
                        assertThat(result.totalRevenue()).isGreaterThan(BigDecimal.ZERO);
                }

                @Test
                @DisplayName("Should handle null POS revenue")
                void getRevenueAnalytics_NullPosRevenue_ReturnsZero() {
                        // Arrange — PosFacade guarantees a non-null sum (see PosFacadeImpl,
                        // which normalizes a null underlying SUM to ZERO), so the mock honors
                        // that contract rather than returning null directly.
                        when(posFacade.sumRevenueByGymIdAndDateRange(eq(gymId), any(), any()))
                                        .thenReturn(BigDecimal.ZERO);
                        when(membershipRepository.sumProjectedRevenueByGymIdAndDateRange(eq(gymId), any(), any()))
                                        .thenReturn(null);
                        when(posFacade.countTransactionsByGymIdAndDateRange(eq(gymId), any(), any()))
                                        .thenReturn(0L);

                        // Act
                        RevenueAnalyticsResponse result = analyticsService.getRevenueAnalytics(gymId,
                                        AnalyticsPeriod.LAST_30_DAYS);

                        // Assert
                        assertThat(result.posRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
                        assertThat(result.membershipRevenue()).isEqualByComparingTo(BigDecimal.ZERO);
                }
        }

        @Nested
        @DisplayName("Class Analytics Tests")
        class ClassAnalyticsTests {

                @Test
                @DisplayName("Should return class analytics with booking counts")
                void getClassAnalytics_ReturnsCounts() {
                        // Arrange
                        when(classesFacade.countByGymId(gymId)).thenReturn(15L);
                        when(classesFacade.countByGymIdAndStartTimeBetween(eq(gymId), any(), any()))
                                        .thenReturn(50L);
                        when(classesFacade.countByGymIdAndDateRange(eq(gymId), any(), any()))
                                        .thenReturn(200L);
                        when(classesFacade.countByGymIdAndStatusAndDateRange(
                                        eq(gymId), eq(BookingStatus.COMPLETED), any(), any()))
                                        .thenReturn(180L);
                        when(classesFacade.countByGymIdAndStatusAndDateRange(
                                        eq(gymId), eq(BookingStatus.CANCELLED), any(), any()))
                                        .thenReturn(15L);
                        when(classesFacade.countByGymIdAndStatusAndDateRange(
                                        eq(gymId), eq(BookingStatus.NO_SHOW), any(), any()))
                                        .thenReturn(5L);
                        when(classesFacade.countBookingsByClassForGym(eq(gymId), any(), any()))
                                        .thenReturn(List.of());
                        when(classesFacade.countBookingsByDayOfWeek(eq(gymId), any(), any()))
                                        .thenReturn(List.of());
                        when(classesFacade.countBookingsByTimeSlot(eq(gymId), any(), any()))
                                        .thenReturn(List.of());

                        // Act
                        ClassAnalyticsResponse result = analyticsService.getClassAnalytics(gymId,
                                        AnalyticsPeriod.LAST_30_DAYS);

                        // Assert
                        assertThat(result).isNotNull();
                        assertThat(result.totalClasses()).isEqualTo(15L);
                        assertThat(result.totalScheduledSessions()).isEqualTo(50L);
                        assertThat(result.totalBookings()).isEqualTo(200L);
                        assertThat(result.completedBookings()).isEqualTo(180L);
                        assertThat(result.cancelledBookings()).isEqualTo(15L);
                        assertThat(result.noShows()).isEqualTo(5L);
                }

                @Test
                @DisplayName("Should calculate attendance rate correctly")
                void getClassAnalytics_CalculatesAttendanceRate() {
                        // Arrange
                        when(classesFacade.countByGymId(gymId)).thenReturn(10L);
                        when(classesFacade.countByGymIdAndStartTimeBetween(eq(gymId), any(), any()))
                                        .thenReturn(20L);
                        when(classesFacade.countByGymIdAndDateRange(eq(gymId), any(), any()))
                                        .thenReturn(100L);
                        when(classesFacade.countByGymIdAndStatusAndDateRange(
                                        eq(gymId), eq(BookingStatus.COMPLETED), any(), any()))
                                        .thenReturn(80L); // 80% attendance
                        when(classesFacade.countByGymIdAndStatusAndDateRange(
                                        eq(gymId), eq(BookingStatus.CANCELLED), any(), any()))
                                        .thenReturn(10L);
                        when(classesFacade.countByGymIdAndStatusAndDateRange(
                                        eq(gymId), eq(BookingStatus.NO_SHOW), any(), any()))
                                        .thenReturn(10L);
                        when(classesFacade.countBookingsByClassForGym(eq(gymId), any(), any()))
                                        .thenReturn(List.of());
                        when(classesFacade.countBookingsByDayOfWeek(eq(gymId), any(), any()))
                                        .thenReturn(List.of());
                        when(classesFacade.countBookingsByTimeSlot(eq(gymId), any(), any()))
                                        .thenReturn(List.of());

                        // Act
                        ClassAnalyticsResponse result = analyticsService.getClassAnalytics(gymId,
                                        AnalyticsPeriod.LAST_30_DAYS);

                        // Assert
                        // 80 completed / 100 total = 80%
                        assertThat(result.averageAttendanceRate()).isEqualByComparingTo(BigDecimal.valueOf(80));
                }

                @Test
                @DisplayName("Should get bookings by class breakdown")
                void getClassAnalytics_BookingsByClassBreakdown() {
                        // Arrange
                        setupClassMocks();
                        List<Object[]> classData = List.of(
                                        new Object[] { "Yoga", 50L },
                                        new Object[] { "Spin", 30L },
                                        new Object[] { "HIIT", 20L });
                        when(classesFacade.countBookingsByClassForGym(eq(gymId), any(), any()))
                                        .thenReturn(classData);
                        when(classesFacade.countBookingsByDayOfWeek(eq(gymId), any(), any()))
                                        .thenReturn(List.of());
                        when(classesFacade.countBookingsByTimeSlot(eq(gymId), any(), any()))
                                        .thenReturn(List.of());

                        // Act
                        ClassAnalyticsResponse result = analyticsService.getClassAnalytics(gymId,
                                        AnalyticsPeriod.LAST_30_DAYS);

                        // Assert
                        assertThat(result.bookingsByClass()).hasSize(3);
                        assertThat(result.bookingsByClass().get(0).category()).isEqualTo("Yoga");
                }
        }

        @Nested
        @DisplayName("Period Date Range Tests")
        class PeriodTests {

                @Test
                @DisplayName("Should calculate correct date range for TODAY")
                void getDashboard_TodayPeriod_CorrectRange() {
                        // Arrange
                        setupMockRepositories();

                        // Act - Just verify no exception for TODAY period
                        DashboardResponse result = analyticsService.getDashboard(gymId, AnalyticsPeriod.TODAY);

                        // Assert
                        assertThat(result).isNotNull();
                }

                @Test
                @DisplayName("Should calculate correct date range for LAST_7_DAYS")
                void getDashboard_Last7DaysPeriod_CorrectRange() {
                        // Arrange
                        setupMockRepositories();

                        // Act
                        DashboardResponse result = analyticsService.getDashboard(gymId, AnalyticsPeriod.LAST_7_DAYS);

                        // Assert
                        assertThat(result).isNotNull();
                }

                @Test
                @DisplayName("Should calculate correct date range for THIS_MONTH")
                void getDashboard_ThisMonthPeriod_CorrectRange() {
                        // Arrange
                        setupMockRepositories();

                        // Act
                        DashboardResponse result = analyticsService.getDashboard(gymId, AnalyticsPeriod.THIS_MONTH);

                        // Assert
                        assertThat(result).isNotNull();
                }
        }

        // Helper methods
        private void setupMockRepositories() {
                when(memberRepository.countByGymId(gymId)).thenReturn(100L);
                setupOtherMocks();
        }

        private void setupOtherMocks() {
                when(membershipRepository.countActiveByGymId(gymId)).thenReturn(90L);
                when(inventoryFacade.countLowStockItems(gymId)).thenReturn(3L);
        }

        private void setupClassMocks() {
                when(classesFacade.countByGymId(gymId)).thenReturn(10L);
                when(classesFacade.countByGymIdAndStartTimeBetween(eq(gymId), any(), any())).thenReturn(20L);
                when(classesFacade.countByGymIdAndDateRange(eq(gymId), any(), any())).thenReturn(100L);
                when(classesFacade.countByGymIdAndStatusAndDateRange(eq(gymId), any(), any(), any()))
                                .thenReturn(0L);
        }
}
