package com.gymmate.unit.subscription.domain;

import com.gymmate.subscription.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Subscription Domain Tests")
class SubscriptionDomainTest {

    @Nested
    @DisplayName("SubscriptionTier Tests")
    class SubscriptionTierTests {

        @Test
        @DisplayName("Should create subscription tier with builder")
        void createTier_WithBuilder_Success() {
            // Arrange & Act
            SubscriptionTier tier = SubscriptionTier.builder()
                    .name("professional")
                    .displayName("Professional Plan")
                    .description("For growing gyms")
                    .price(new BigDecimal("49.99"))
                    .billingCycle("monthly")
                    .active(true)
                    .maxMembers(500)
                    .maxStaff(20)
                    .build();

            // Assert
            assertThat(tier.getName()).isEqualTo("professional");
            assertThat(tier.getDisplayName()).isEqualTo("Professional Plan");
            assertThat(tier.getPrice()).isEqualByComparingTo(new BigDecimal("49.99"));
            assertThat(tier.getMaxMembers()).isEqualTo(500);
            assertThat(tier.getActive()).isTrue();
        }

        @Test
        @DisplayName("Should have active defaulted to true")
        void createTier_DefaultActiveIsTrue() {
            // Arrange - builder defaults active to true
            SubscriptionTier tier = SubscriptionTier.builder()
                    .name("test-tier")
                    .displayName("Test Tier")
                    .price(new BigDecimal("29.99"))
                    .maxMembers(100)
                    .build();

            // Assert - verify default is true
            assertThat(tier.getActive()).isTrue();
        }
    }

    @Nested
    @DisplayName("GymSubscription Tests")
    class GymSubscriptionTests {

        @Test
        @DisplayName("Should create gym subscription with builder")
        void createSubscription_WithBuilder_Success() {
            // Arrange
            UUID organisationId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            // Act
            Subscription subscription = Subscription.builder()
                    .organisationId(organisationId)
                    .status(SubscriptionStatus.ACTIVE)
                    .currentPeriodStart(now)
                    .currentPeriodEnd(now.plusMonths(1))
                    .build();

            // Assert
            assertThat(subscription.getOrganisationId()).isEqualTo(organisationId);
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.ACTIVE);
        }

        @Test
        @DisplayName("Should track Stripe subscription ID")
        void setStripeSubscriptionId_ValidId_Stored() {
            // Arrange
            Subscription subscription = Subscription.builder()
                    .stripeSubscriptionId("sub_test123")
                    .build();

            // Assert
            assertThat(subscription.getStripeSubscriptionId()).isEqualTo("sub_test123");
        }

        @Test
        @DisplayName("Should track trial period")
        void setTrialEnd_ValidDate_Stored() {
            // Arrange
            LocalDateTime trialEnd = LocalDateTime.now().plusDays(14);

            Subscription subscription = Subscription.builder()
                    .trialEnd(trialEnd)
                    .build();

            // Assert
            assertThat(subscription.getTrialEnd()).isEqualTo(trialEnd);
        }

        @Test
        @DisplayName("Should cancel subscription")
        void cancelSubscription_ShouldUpdateStatus() {
            // Arrange
            Subscription subscription = Subscription.builder()
                    .status(SubscriptionStatus.ACTIVE)
                    .build();

            // Act
            subscription.setStatus(SubscriptionStatus.CANCELLED);
            subscription.setCancelledAt(LocalDateTime.now());

            // Assert
            assertThat(subscription.getStatus()).isEqualTo(SubscriptionStatus.CANCELLED);
            assertThat(subscription.getCancelledAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("SubscriptionStatus Tests")
    class SubscriptionStatusTests {

        @ParameterizedTest
        @EnumSource(SubscriptionStatus.class)
        @DisplayName("Should have all expected statuses")
        void allStatuses_ShouldExist(SubscriptionStatus status) {
            assertThat(status).isNotNull();
        }

        @Test
        @DisplayName("Should have correct status values")
        void statusValues_ShouldBeCorrect() {
            assertThat(SubscriptionStatus.values())
                    .contains(
                            SubscriptionStatus.ACTIVE,
                            SubscriptionStatus.TRIAL,
                            SubscriptionStatus.PAST_DUE,
                            SubscriptionStatus.CANCELLED,
                            SubscriptionStatus.EXPIRED,
                            SubscriptionStatus.SUSPENDED
                    );
        }
    }

    @Nested
    @DisplayName("SubscriptionUsage Tests")
    class SubscriptionUsageTests {

        @Test
        @DisplayName("Should track subscription usage with builder")
        void createUsage_WithBuilder_Success() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            SubscriptionUsage usage = SubscriptionUsage.builder()
                    .billingPeriodStart(now)
                    .billingPeriodEnd(now.plusMonths(1))
                    .memberCount(150)
                    .smsSent(100)
                    .emailSent(500)
                    .apiRequests(10000)
                    .build();

            // Assert
            assertThat(usage.getMemberCount()).isEqualTo(150);
            assertThat(usage.getSmsSent()).isEqualTo(100);
            assertThat(usage.getEmailSent()).isEqualTo(500);
            assertThat(usage.getApiRequests()).isEqualTo(10000);
        }

        @Test
        @DisplayName("Should track overage")
        void trackOverage_SetsCorrectValues() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();

            // Act
            SubscriptionUsage usage = SubscriptionUsage.builder()
                    .billingPeriodStart(now)
                    .billingPeriodEnd(now.plusMonths(1))
                    .memberCount(120)
                    .memberOverage(20)
                    .smsOverage(50)
                    .build();

            // Assert
            assertThat(usage.getMemberOverage()).isEqualTo(20);
            assertThat(usage.getSmsOverage()).isEqualTo(50);
        }
    }

    @Nested
    @DisplayName("ApiRateLimit Tests")
    class ApiRateLimitTests {

        @Test
        @DisplayName("Should create rate limit config with builder")
        void createRateLimit_WithBuilder_Success() {
            // Arrange
            UUID organisationId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            // Act
            ApiRateLimit rateLimit = ApiRateLimit.builder()
                    .organisationId(organisationId)
                    .windowStart(now)
                    .windowEnd(now.plusHours(1))
                    .windowType("hourly")
                    .requestCount(100)
                    .limitThreshold(1000)
                    .endpointPath("/api/members")
                    .build();

            // Assert
            assertThat(rateLimit.getOrganisationId()).isEqualTo(organisationId);
            assertThat(rateLimit.getRequestCount()).isEqualTo(100);
            assertThat(rateLimit.getLimitThreshold()).isEqualTo(1000);
            assertThat(rateLimit.getEndpointPath()).isEqualTo("/api/members");
        }

        @Test
        @DisplayName("Should track rate limit hits")
        void trackRateLimitHits_UpdatesCount() {
            // Arrange
            LocalDateTime now = LocalDateTime.now();
            ApiRateLimit rateLimit = ApiRateLimit.builder()
                    .windowStart(now)
                    .windowEnd(now.plusHours(1))
                    .requestCount(0)
                    .limitThreshold(100)
                    .build();

            // Act
            rateLimit.setRequestCount(rateLimit.getRequestCount() + 1);

            // Assert
            assertThat(rateLimit.getRequestCount()).isEqualTo(1);
        }
    }
}
