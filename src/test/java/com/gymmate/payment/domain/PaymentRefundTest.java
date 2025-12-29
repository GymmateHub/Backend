package com.gymmate.payment.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("PaymentRefund Entity Tests")
class PaymentRefundTest {

    @Nested
    @DisplayName("Status Update Tests")
    class StatusUpdateTests {

        @Test
        @DisplayName("Should update status")
        void updateStatus_Success() {
            // Arrange
            PaymentRefund refund = createRefund();

            // Act
            refund.updateStatus(RefundStatus.SUCCEEDED);

            // Assert
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.SUCCEEDED);
        }

        @Test
        @DisplayName("Should mark as failed with reason")
        void markFailed_SetsStatusAndReason() {
            // Arrange
            PaymentRefund refund = createRefund();

            // Act
            refund.markFailed("Insufficient funds");

            // Assert
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.FAILED);
            assertThat(refund.getFailureReason()).isEqualTo("Insufficient funds");
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build with all fields")
        void build_AllFields_Success() {
            // Arrange
            UUID gymId = UUID.randomUUID();
            UUID subscriptionId = UUID.randomUUID();
            UUID refundToUserId = UUID.randomUUID();
            UUID processedByUserId = UUID.randomUUID();

            // Act
            PaymentRefund refund = PaymentRefund.builder()
                    .gymId(gymId)
                    .stripeRefundId("re_test123")
                    .stripePaymentIntentId("pi_test456")
                    .stripeChargeId("ch_test789")
                    .amount(new BigDecimal("50.00"))
                    .currency("USD")
                    .status(RefundStatus.SUCCEEDED)
                    .reason("duplicate")
                    .customReason("Customer charged twice")
                    .subscriptionId(subscriptionId)
                    .refundType(RefundType.MEMBER_PAYMENT)
                    .refundToUserId(refundToUserId)
                    .refundToType("MEMBER")
                    .processedByUserId(processedByUserId)
                    .processedByType("GYM_OWNER")
                    .build();

            // Assert
            assertThat(refund.getGymId()).isEqualTo(gymId);
            assertThat(refund.getStripeRefundId()).isEqualTo("re_test123");
            assertThat(refund.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.SUCCEEDED);
            assertThat(refund.getRefundType()).isEqualTo(RefundType.MEMBER_PAYMENT);
            assertThat(refund.getRefundToUserId()).isEqualTo(refundToUserId);
            assertThat(refund.getProcessedByUserId()).isEqualTo(processedByUserId);
        }

        @Test
        @DisplayName("Should use default values")
        void build_DefaultValues_Applied() {
            // Act
            PaymentRefund refund = PaymentRefund.builder()
                    .gymId(UUID.randomUUID())
                    .stripeRefundId("re_test")
                    .stripePaymentIntentId("pi_test")
                    .amount(new BigDecimal("25.00"))
                    .status(RefundStatus.PENDING)
                    .build();

            // Assert defaults
            assertThat(refund.getCurrency()).isEqualTo("USD");
            assertThat(refund.getRefundType()).isEqualTo(RefundType.PLATFORM_SUBSCRIPTION);
            assertThat(refund.getRequestedByType()).isEqualTo("user");
        }
    }

    private PaymentRefund createRefund() {
        return PaymentRefund.builder()
                .gymId(UUID.randomUUID())
                .stripeRefundId("re_test123")
                .stripePaymentIntentId("pi_test456")
                .amount(new BigDecimal("100.00"))
                .status(RefundStatus.PENDING)
                .build();
    }
}

@DisplayName("RefundStatus Enum Tests")
class RefundStatusTest {

    @ParameterizedTest
    @CsvSource({
            "succeeded, SUCCEEDED",
            "failed, FAILED",
            "canceled, CANCELED",
            "requires_action, REQUIRES_ACTION",
            "pending, PENDING",
            "unknown_status, PENDING"
    })
    @DisplayName("Should map Stripe status correctly")
    void fromStripeStatus_MapsCorrectly(String stripeStatus, RefundStatus expected) {
        assertThat(RefundStatus.fromStripeStatus(stripeStatus)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should handle null Stripe status")
    void fromStripeStatus_Null_ReturnsPending() {
        assertThat(RefundStatus.fromStripeStatus(null)).isEqualTo(RefundStatus.PENDING);
    }

    @Test
    @DisplayName("Should handle uppercase Stripe status")
    void fromStripeStatus_Uppercase_MapsCorrectly() {
        assertThat(RefundStatus.fromStripeStatus("SUCCEEDED")).isEqualTo(RefundStatus.SUCCEEDED);
    }
}

@DisplayName("RefundRequestStatus Enum Tests")
class RefundRequestStatusTest {

    @Test
    @DisplayName("Should have all expected values")
    void shouldHaveAllExpectedValues() {
        RefundRequestStatus[] values = RefundRequestStatus.values();
        assertThat(values).containsExactlyInAnyOrder(
                RefundRequestStatus.PENDING,
                RefundRequestStatus.UNDER_REVIEW,
                RefundRequestStatus.APPROVED,
                RefundRequestStatus.REJECTED,
                RefundRequestStatus.PROCESSED,
                RefundRequestStatus.CANCELLED
        );
    }
}

@DisplayName("RefundType Enum Tests")
class RefundTypeTest {

    @Test
    @DisplayName("Should have PLATFORM_SUBSCRIPTION type")
    void shouldHavePlatformSubscriptionType() {
        assertThat(RefundType.PLATFORM_SUBSCRIPTION).isNotNull();
    }

    @Test
    @DisplayName("Should have MEMBER_PAYMENT type")
    void shouldHaveMemberPaymentType() {
        assertThat(RefundType.MEMBER_PAYMENT).isNotNull();
    }
}

@DisplayName("RefundReasonCategory Enum Tests")
class RefundReasonCategoryTest {

    @Test
    @DisplayName("Should have all expected categories")
    void shouldHaveAllExpectedCategories() {
        RefundReasonCategory[] values = RefundReasonCategory.values();
        assertThat(values).contains(
                RefundReasonCategory.SERVICE_NOT_PROVIDED,
                RefundReasonCategory.DUPLICATE_CHARGE,
                RefundReasonCategory.DISSATISFIED,
                RefundReasonCategory.CANCELLED_MEMBERSHIP,
                RefundReasonCategory.CLASS_CANCELLED,
                RefundReasonCategory.BILLING_ERROR,
                RefundReasonCategory.OTHER
        );
    }
}

