package com.gymmate.unit.payment.domain;

import com.gymmate.payment.domain.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Payment Refund Domain Tests")
class PaymentRefundTest {

    @Nested
    @DisplayName("PaymentRefund Creation Tests")
    class PaymentRefundCreationTests {

        @Test
        @DisplayName("Should create refund with builder")
        void builder_WithValidData_Success() {
            // Arrange
            UUID gymId = UUID.randomUUID();

            // Act
            PaymentRefund refund = PaymentRefund.builder()
                    .gymId(gymId)
                    .stripeRefundId("re_test123")
                    .stripePaymentIntentId("pi_test123")
                    .amount(new BigDecimal("50.00"))
                    .currency("usd")
                    .reason("Customer request")
                    .status(RefundStatus.PENDING)
                    .refundType(RefundType.MEMBER_PAYMENT)
                    .build();

            // Assert
            assertThat(refund.getGymId()).isEqualTo(gymId);
            assertThat(refund.getStripeRefundId()).isEqualTo("re_test123");
            assertThat(refund.getAmount()).isEqualByComparingTo(new BigDecimal("50.00"));
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.PENDING);
        }

        @Test
        @DisplayName("Should create refund with refunded user info")
        void builder_WithRefundedUserInfo_Success() {
            // Arrange
            UUID gymId = UUID.randomUUID();
            UUID memberId = UUID.randomUUID();
            UUID processedById = UUID.randomUUID();

            // Act
            PaymentRefund refund = PaymentRefund.builder()
                    .gymId(gymId)
                    .stripeRefundId("re_test456")
                    .stripePaymentIntentId("pi_test456")
                    .amount(new BigDecimal("25.00"))
                    .currency("usd")
                    .status(RefundStatus.SUCCEEDED)
                    .refundType(RefundType.MEMBER_PAYMENT)
                    .refundToUserId(memberId)
                    .refundToType("MEMBER")
                    .processedByUserId(processedById)
                    .processedByType("GYM_OWNER")
                    .build();

            // Assert
            assertThat(refund.getRefundToUserId()).isEqualTo(memberId);
            assertThat(refund.getRefundToType()).isEqualTo("MEMBER");
            assertThat(refund.getProcessedByUserId()).isEqualTo(processedById);
            assertThat(refund.getProcessedByType()).isEqualTo("GYM_OWNER");
        }
    }

    @Nested
    @DisplayName("RefundStatus Tests")
    class RefundStatusTests {

        @ParameterizedTest
        @EnumSource(RefundStatus.class)
        @DisplayName("Should have all expected refund statuses")
        void allStatuses_ShouldExist(RefundStatus status) {
            // Assert
            assertThat(status).isNotNull();
        }

        @Test
        @DisplayName("Should have expected status values")
        void statusValues_ShouldBeCorrect() {
            assertThat(RefundStatus.values())
                    .contains(
                            RefundStatus.PENDING,
                            RefundStatus.SUCCEEDED,
                            RefundStatus.FAILED,
                            RefundStatus.CANCELED
                    );
        }
    }

    @Nested
    @DisplayName("RefundType Tests")
    class RefundTypeTests {

        @ParameterizedTest
        @EnumSource(RefundType.class)
        @DisplayName("Should have all expected refund types")
        void allTypes_ShouldExist(RefundType type) {
            // Assert
            assertThat(type).isNotNull();
        }

        @Test
        @DisplayName("Should have expected type values")
        void typeValues_ShouldBeCorrect() {
            assertThat(RefundType.values())
                    .contains(
                            RefundType.PLATFORM_SUBSCRIPTION,
                            RefundType.MEMBER_PAYMENT
                    );
        }
    }

    @Nested
    @DisplayName("Refund Amount Tests")
    class RefundAmountTests {

        @Test
        @DisplayName("Should handle full refund amount")
        void fullRefund_SetsCorrectAmount() {
            // Arrange
            BigDecimal originalAmount = new BigDecimal("100.00");

            // Act
            PaymentRefund refund = PaymentRefund.builder()
                    .stripeRefundId("re_test")
                    .stripePaymentIntentId("pi_test")
                    .amount(originalAmount)
                    .currency("usd")
                    .status(RefundStatus.PENDING)
                    .build();

            // Assert
            assertThat(refund.getAmount()).isEqualByComparingTo(originalAmount);
        }

        @Test
        @DisplayName("Should handle partial refund amount")
        void partialRefund_SetsCorrectAmount() {
            // Arrange
            BigDecimal partialAmount = new BigDecimal("30.00");

            // Act
            PaymentRefund refund = PaymentRefund.builder()
                    .stripeRefundId("re_test")
                    .stripePaymentIntentId("pi_test")
                    .amount(partialAmount)
                    .currency("usd")
                    .status(RefundStatus.PENDING)
                    .build();

            // Assert
            assertThat(refund.getAmount()).isEqualByComparingTo(partialAmount);
        }
    }

    @Nested
    @DisplayName("Refund Status Transitions")
    class RefundStatusTransitionsTests {

        @Test
        @DisplayName("Should update status to succeeded")
        void updateStatus_ToSucceeded_Works() {
            // Arrange
            PaymentRefund refund = PaymentRefund.builder()
                    .stripeRefundId("re_test")
                    .stripePaymentIntentId("pi_test")
                    .amount(new BigDecimal("50.00"))
                    .status(RefundStatus.PENDING)
                    .build();

            // Act
            refund.setStatus(RefundStatus.SUCCEEDED);

            // Assert
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.SUCCEEDED);
        }

        @Test
        @DisplayName("Should update status to failed with reason")
        void updateStatus_ToFailed_Works() {
            // Arrange
            PaymentRefund refund = PaymentRefund.builder()
                    .stripeRefundId("re_test")
                    .stripePaymentIntentId("pi_test")
                    .amount(new BigDecimal("50.00"))
                    .status(RefundStatus.PENDING)
                    .build();

            // Act
            refund.setStatus(RefundStatus.FAILED);
            refund.setFailureReason("Insufficient funds");

            // Assert
            assertThat(refund.getStatus()).isEqualTo(RefundStatus.FAILED);
            assertThat(refund.getFailureReason()).isEqualTo("Insufficient funds");
        }
    }

    @Nested
    @DisplayName("Refund Tracking Tests")
    class RefundTrackingTests {

        @Test
        @DisplayName("Should track who requested the refund")
        void trackRequester_SetsCorrectInfo() {
            // Arrange
            UUID requesterId = UUID.randomUUID();

            // Act
            PaymentRefund refund = PaymentRefund.builder()
                    .stripeRefundId("re_test")
                    .stripePaymentIntentId("pi_test")
                    .amount(new BigDecimal("50.00"))
                    .status(RefundStatus.PENDING)
                    .requestedBy(requesterId)
                    .requestedByType("MEMBER")
                    .build();

            // Assert
            assertThat(refund.getRequestedBy()).isEqualTo(requesterId);
            assertThat(refund.getRequestedByType()).isEqualTo("MEMBER");
        }

        @Test
        @DisplayName("Should link to subscription")
        void linkToSubscription_SetsId() {
            // Arrange
            UUID subscriptionId = UUID.randomUUID();

            // Act
            PaymentRefund refund = PaymentRefund.builder()
                    .stripeRefundId("re_test")
                    .stripePaymentIntentId("pi_test")
                    .amount(new BigDecimal("50.00"))
                    .status(RefundStatus.PENDING)
                    .subscriptionId(subscriptionId)
                    .build();

            // Assert
            assertThat(refund.getSubscriptionId()).isEqualTo(subscriptionId);
        }

        @Test
        @DisplayName("Should link to invoice")
        void linkToInvoice_SetsId() {
            // Arrange
            UUID invoiceId = UUID.randomUUID();

            // Act
            PaymentRefund refund = PaymentRefund.builder()
                    .stripeRefundId("re_test")
                    .stripePaymentIntentId("pi_test")
                    .amount(new BigDecimal("50.00"))
                    .status(RefundStatus.PENDING)
                    .invoiceId(invoiceId)
                    .build();

            // Assert
            assertThat(refund.getInvoiceId()).isEqualTo(invoiceId);
        }
    }
}
