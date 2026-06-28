package com.gymmate.unit.payment.application;

import com.gymmate.notification.events.ChargeDisputedEvent;
import com.gymmate.notification.events.ChargeRefundedEvent;
import com.gymmate.notification.events.SubscriptionPausedEvent;
import com.gymmate.shared.constants.InvoiceStatus;
import com.gymmate.shared.constants.SubscriptionStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Unit tests for the new Stripe webhook handlers:
 * - customer.subscription.paused
 * - charge.disputed
 * - charge.refunded
 */
@DisplayName("StripeWebhookService New Handlers Tests")
class StripeWebhookNewHandlersTest {

    private UUID organisationId;

    @BeforeEach
    void setUp() {
        organisationId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("SubscriptionStatus Mapping")
    class SubscriptionStatusMapping {

        @Test
        @DisplayName("PAUSED status should exist in SubscriptionStatus enum")
        void pausedStatusShouldExist() {
            SubscriptionStatus paused = SubscriptionStatus.PAUSED;
            assertThat(paused).isNotNull();
            assertThat(paused.isPaused()).isTrue();
            assertThat(paused.isActive()).isFalse();
            assertThat(paused.canAccess()).isFalse();
        }
    }

    @Nested
    @DisplayName("InvoiceStatus REFUNDED")
    class InvoiceStatusRefunded {

        @Test
        @DisplayName("REFUNDED status should exist in InvoiceStatus enum")
        void refundedStatusShouldExist() {
            InvoiceStatus refunded = InvoiceStatus.REFUNDED;
            assertThat(refunded).isNotNull();
            assertThat(refunded.name()).isEqualTo("REFUNDED");
        }
    }

    @Nested
    @DisplayName("Domain Event Construction")
    class DomainEventConstruction {

        @Test
        @DisplayName("ChargeDisputedEvent should build with correct fields")
        void chargeDisputedEventShouldBuild() {
            ChargeDisputedEvent event = ChargeDisputedEvent.builder()
                    .organisationId(organisationId)
                    .amount(java.math.BigDecimal.valueOf(50.00))
                    .currency("USD")
                    .disputeId("dp_test123")
                    .disputeReason("fraudulent")
                    .paymentIntentId("pi_test123")
                    .build();

            assertThat(event.getEventId()).isNotNull();
            assertThat(event.getOccurredAt()).isNotNull();
            assertThat(event.getOrganisationId()).isEqualTo(organisationId);
            assertThat(event.getEventType()).isEqualTo("CHARGE_DISPUTED");
            assertThat(event.getNotificationTitle()).contains("Dispute");
            assertThat(event.getNotificationMessage()).contains("50");
            assertThat(event.getNotificationMessage()).contains("fraudulent");
            assertThat(event.getPriority()).isEqualTo(com.gymmate.shared.constants.NotificationPriority.CRITICAL);
        }

        @Test
        @DisplayName("ChargeRefundedEvent should build with correct fields")
        void chargeRefundedEventShouldBuild() {
            ChargeRefundedEvent event = ChargeRefundedEvent.builder()
                    .organisationId(organisationId)
                    .amount(java.math.BigDecimal.valueOf(25.00))
                    .currency("EUR")
                    .refundId("re_test456")
                    .paymentIntentId("pi_test456")
                    .reason("requested_by_customer")
                    .build();

            assertThat(event.getEventType()).isEqualTo("CHARGE_REFUNDED");
            assertThat(event.getNotificationTitle()).contains("Refund");
            assertThat(event.getNotificationMessage()).contains("25");
            assertThat(event.getNotificationMessage()).contains("EUR");
            assertThat(event.getPriority()).isEqualTo(com.gymmate.shared.constants.NotificationPriority.HIGH);
        }

        @Test
        @DisplayName("SubscriptionPausedEvent should build with correct fields")
        void subscriptionPausedEventShouldBuild() {
            UUID subId = UUID.randomUUID();
            SubscriptionPausedEvent event = SubscriptionPausedEvent.builder()
                    .organisationId(organisationId)
                    .subscriptionId(subId)
                    .tierName("Pro")
                    .build();

            assertThat(event.getEventType()).isEqualTo("SUBSCRIPTION_PAUSED");
            assertThat(event.getNotificationTitle()).contains("Paused");
            assertThat(event.getNotificationMessage()).contains("Pro");
            assertThat(event.getPriority()).isEqualTo(com.gymmate.shared.constants.NotificationPriority.HIGH);
        }

        @Test
        @DisplayName("Events should handle null fields gracefully in messages")
        void eventsShouldHandleNullFields() {
            ChargeDisputedEvent event = ChargeDisputedEvent.builder()
                    .organisationId(organisationId)
                    .build();

            // Should not throw NPE
            String message = event.getNotificationMessage();
            assertThat(message).contains("unknown");
            assertThat(message).contains("Not specified");
        }
    }
}
