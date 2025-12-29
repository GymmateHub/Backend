package com.gymmate.unit.membership.domain;

import com.gymmate.membership.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Membership Domain Tests")
class MembershipDomainTest {

    @Nested
    @DisplayName("MembershipPlan Tests")
    class MembershipPlanTests {

        @Test
        @DisplayName("Should create membership plan with builder")
        void createPlan_WithBuilder_Success() {
            // Arrange
            UUID gymId = UUID.randomUUID();

            // Act
            MembershipPlan plan = MembershipPlan.builder()
                    .name("Premium Monthly")
                    .description("Full access to all facilities")
                    .price(new BigDecimal("49.99"))
                    .billingCycle("monthly")
                    .durationMonths(1)
                    .classCredits(10)
                    .peakHoursAccess(true)
                    .build();

            // Assert
            assertThat(plan.getName()).isEqualTo("Premium Monthly");
            assertThat(plan.getPrice()).isEqualByComparingTo(new BigDecimal("49.99"));
            assertThat(plan.getDurationMonths()).isEqualTo(1);
            assertThat(plan.getClassCredits()).isEqualTo(10);
        }

        @Test
        @DisplayName("Should handle annual plan")
        void createPlan_Annual_CorrectDuration() {
            // Act
            MembershipPlan plan = MembershipPlan.builder()
                    .name("Annual Plan")
                    .price(new BigDecimal("499.99"))
                    .billingCycle("yearly")
                    .durationMonths(12)
                    .build();

            // Assert
            assertThat(plan.getDurationMonths()).isEqualTo(12);
            assertThat(plan.getBillingCycle()).isEqualTo("yearly");
        }

        @Test
        @DisplayName("Should set peak hours restriction")
        void setPeakHours_OffPeakOnly_Restricted() {
            // Act
            MembershipPlan plan = MembershipPlan.builder()
                    .name("Off-Peak Plan")
                    .price(new BigDecimal("29.99"))
                    .billingCycle("monthly")
                    .peakHoursAccess(false)
                    .offPeakOnly(true)
                    .build();

            // Assert
            assertThat(plan.isPeakHoursAccess()).isFalse();
            assertThat(plan.isOffPeakOnly()).isTrue();
        }
    }

    @Nested
    @DisplayName("MemberMembership Tests")
    class MemberMembershipTests {

        @Test
        @DisplayName("Should create member membership with builder")
        void createMembership_WithBuilder_Success() {
            // Arrange
            UUID memberId = UUID.randomUUID();
            LocalDate startDate = LocalDate.now();

            // Act
            MemberMembership membership = MemberMembership.builder()
                    .memberId(memberId)
                    .status(MembershipStatus.ACTIVE)
                    .startDate(startDate)
                    .endDate(startDate.plusMonths(1))
                    .autoRenew(true)
                    .build();

            // Assert
            assertThat(membership.getMemberId()).isEqualTo(memberId);
            assertThat(membership.getStatus()).isEqualTo(MembershipStatus.ACTIVE);
            assertThat(membership.isAutoRenew()).isTrue();
        }

        @Test
        @DisplayName("Should cancel membership")
        void cancelMembership_ShouldUpdateStatus() {
            // Arrange
            MemberMembership membership = MemberMembership.builder()
                    .status(MembershipStatus.ACTIVE)
                    .build();

            // Act
            membership.setStatus(MembershipStatus.CANCELLED);

            // Assert
            assertThat(membership.getStatus()).isEqualTo(MembershipStatus.CANCELLED);
        }

        @Test
        @DisplayName("Should pause membership")
        void pauseMembership_ShouldUpdateStatus() {
            // Arrange
            MemberMembership membership = MemberMembership.builder()
                    .status(MembershipStatus.ACTIVE)
                    .build();

            // Act
            membership.setStatus(MembershipStatus.PAUSED);

            // Assert
            assertThat(membership.getStatus()).isEqualTo(MembershipStatus.PAUSED);
        }
    }

    @Nested
    @DisplayName("MembershipStatus Tests")
    class MembershipStatusTests {

        @ParameterizedTest
        @EnumSource(MembershipStatus.class)
        @DisplayName("Should have all expected membership statuses")
        void allStatuses_ShouldExist(MembershipStatus status) {
            assertThat(status).isNotNull();
        }

        @Test
        @DisplayName("Should have correct status values")
        void statusValues_ShouldBeCorrect() {
            assertThat(MembershipStatus.values())
                    .contains(
                            MembershipStatus.ACTIVE,
                            MembershipStatus.PAUSED,
                            MembershipStatus.CANCELLED,
                            MembershipStatus.EXPIRED
                    );
        }
    }
}

