package com.gymmate.payment.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("GymInvoice Entity Tests")
class GymInvoiceTest {

    @Nested
    @DisplayName("Status Transition Tests")
    class StatusTransitionTests {

        @Test
        @DisplayName("Should mark invoice as paid")
        void markPaid_Success() {
            // Arrange
            GymInvoice invoice = createInvoice(InvoiceStatus.OPEN);
            LocalDateTime paidAt = LocalDateTime.now();

            // Act
            invoice.markPaid(paidAt);

            // Assert
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAID);
            assertThat(invoice.getPaidAt()).isEqualTo(paidAt);
        }

        @Test
        @DisplayName("Should mark invoice as failed")
        void markFailed_Success() {
            // Arrange
            GymInvoice invoice = createInvoice(InvoiceStatus.OPEN);

            // Act
            invoice.markFailed();

            // Assert
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.PAYMENT_FAILED);
        }

        @Test
        @DisplayName("Should mark invoice as void")
        void markVoid_Success() {
            // Arrange
            GymInvoice invoice = createInvoice(InvoiceStatus.OPEN);

            // Act
            invoice.markVoid();

            // Assert
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.VOID);
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should build with all fields")
        void build_AllFields_Success() {
            // Arrange
            UUID organisationId = UUID.randomUUID();
            LocalDateTime periodStart = LocalDateTime.now().minusMonths(1);
            LocalDateTime periodEnd = LocalDateTime.now();
            LocalDateTime dueDate = LocalDateTime.now().plusDays(30);

            // Act
            GymInvoice invoice = GymInvoice.builder()
                    .organisationId(organisationId)
                    .stripeInvoiceId("in_test123")
                    .invoiceNumber("INV-001")
                    .amount(new BigDecimal("99.99"))
                    .currency("USD")
                    .status(InvoiceStatus.OPEN)
                    .description("Monthly subscription")
                    .periodStart(periodStart)
                    .periodEnd(periodEnd)
                    .dueDate(dueDate)
                    .invoicePdfUrl("https://stripe.com/invoice.pdf")
                    .hostedInvoiceUrl("https://stripe.com/invoice")
                    .build();

            // Assert
            assertThat(invoice.getOrganisationId()).isEqualTo(organisationId);
            assertThat(invoice.getStripeInvoiceId()).isEqualTo("in_test123");
            assertThat(invoice.getAmount()).isEqualByComparingTo(new BigDecimal("99.99"));
            assertThat(invoice.getCurrency()).isEqualTo("USD");
            assertThat(invoice.getStatus()).isEqualTo(InvoiceStatus.OPEN);
        }

        @Test
        @DisplayName("Should use default currency USD")
        void build_DefaultCurrency_Applied() {
            // Act
            GymInvoice invoice = GymInvoice.builder()
                    .organisationId(UUID.randomUUID())
                    .amount(new BigDecimal("50.00"))
                    .status(InvoiceStatus.DRAFT)
                    .build();

            // Assert
            assertThat(invoice.getCurrency()).isEqualTo("USD");
        }
    }

    // Helper method
    private GymInvoice createInvoice(InvoiceStatus status) {
        return GymInvoice.builder()
                .organisationId(UUID.randomUUID())
                .stripeInvoiceId("in_test123")
                .amount(new BigDecimal("99.99"))
                .status(status)
                .build();
    }
}

@DisplayName("InvoiceStatus Enum Tests")
class InvoiceStatusTest {

    @ParameterizedTest
    @CsvSource({
            "draft, DRAFT",
            "open, OPEN",
            "paid, PAID",
            "void, VOID",
            "uncollectible, UNCOLLECTIBLE",
            "unknown, OPEN"
    })
    @DisplayName("Should map Stripe status correctly")
    void fromStripeStatus_MapsCorrectly(String stripeStatus, InvoiceStatus expected) {
        assertThat(InvoiceStatus.fromStripeStatus(stripeStatus)).isEqualTo(expected);
    }

    @Test
    @DisplayName("Should have all expected values")
    void shouldHaveAllExpectedValues() {
        InvoiceStatus[] values = InvoiceStatus.values();
        assertThat(values).containsExactlyInAnyOrder(
                InvoiceStatus.DRAFT,
                InvoiceStatus.OPEN,
                InvoiceStatus.PAID,
                InvoiceStatus.PAYMENT_FAILED,
                InvoiceStatus.VOID,
                InvoiceStatus.UNCOLLECTIBLE);
    }
}

@DisplayName("PaymentMethod Entity Tests")
class PaymentMethodEntityTest {

    @Test
    @DisplayName("Should build organisation payment method with card details")
    void build_OrganisationPaymentMethod_Success() {
        // Arrange & Act
        UUID organisationId = UUID.randomUUID();
        UUID gymId = UUID.randomUUID();
        PaymentMethod method = PaymentMethod.forOrganisation(organisationId, gymId, "pm_test123",
                PaymentMethodType.CARD);
        method.setCardBrand("visa");
        method.setCardLastFour("4242");
        method.setCardExpiresMonth(12);
        method.setCardExpiresYear(2025);
        method.setIsDefault(true);

        // Assert
        assertThat(method.getOwnerType()).isEqualTo(PaymentMethodOwnerType.ORGANISATION);
        assertThat(method.getOwnerId()).isEqualTo(organisationId);
        assertThat(method.getOrganisationId()).isEqualTo(organisationId);
        assertThat(method.getMemberId()).isNull();
        assertThat(method.getMethodType()).isEqualTo(PaymentMethodType.CARD);
        assertThat(method.getCardBrand()).isEqualTo("visa");
        assertThat(method.getLastFour()).isEqualTo("4242");
        assertThat(method.getExpiryMonth()).isEqualTo(12);
        assertThat(method.getExpiryYear()).isEqualTo(2025);
        assertThat(method.getIsDefault()).isTrue();
        assertThat(method.isOrganisationPaymentMethod()).isTrue();
    }

    @Test
    @DisplayName("Should build member payment method")
    void build_MemberPaymentMethod_Success() {
        // Arrange & Act
        UUID memberId = UUID.randomUUID();
        UUID gymId = UUID.randomUUID();
        PaymentMethod method = PaymentMethod.forMember(memberId, gymId, "pm_test456", PaymentMethodType.CARD);

        // Assert
        assertThat(method.getOwnerType()).isEqualTo(PaymentMethodOwnerType.MEMBER);
        assertThat(method.getOwnerId()).isEqualTo(memberId);
        assertThat(method.getGymId()).isEqualTo(gymId);
        assertThat(method.getMemberId()).isEqualTo(memberId);
        assertThat(method.isOrganisationPaymentMethod()).isFalse();
    }

    @Test
    @DisplayName("Should use default values")
    void build_DefaultValues_Applied() {
        // Act
        PaymentMethod method = PaymentMethod.forOrganisation(UUID.randomUUID(), UUID.randomUUID(), "pm_test",
                PaymentMethodType.CARD);

        // Assert
        assertThat(method.getIsDefault()).isFalse();
        assertThat(method.getIsActive()).isTrue();
        assertThat(method.getIsVerified()).isFalse();
        assertThat(method.getProvider()).isEqualTo("stripe");
    }
}
