package com.gymmate.unit.notification.application;

import com.gymmate.notification.api.event.MemberJoinedEvent;
import com.gymmate.notification.api.event.PaymentFailedEvent;
import com.gymmate.notification.api.event.PaymentSuccessEvent;
import com.gymmate.notification.api.event.SubscriptionExpiringEvent;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import com.gymmate.notification.internal.infrastructure.messaging.AdminNotificationEventListener;
import com.gymmate.notification.internal.application.NotificationDispatcher;
import com.gymmate.notification.internal.domain.Notification;
import com.gymmate.notification.internal.application.port.NotificationRepository;
import com.gymmate.shared.constants.NotificationPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AdminNotificationEventListener Unit Tests")
class AdminNotificationEventListenerTest {

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private NotificationDispatcher notificationDispatcher;

    @Spy
    private ObjectMapper objectMapper = new JsonMapper();

    private AdminNotificationEventListener listener;

    private UUID organisationId;
    private UUID gymId;

    @BeforeEach
    void setUp() {
        listener = new AdminNotificationEventListener(
                notificationRepository,
                notificationDispatcher,
                objectMapper);
        organisationId = UUID.randomUUID();
        gymId = UUID.randomUUID();

        // Default stub: return the notification that was passed in, so saved.getId()
        // doesn't NPE
        lenient().when(notificationRepository.save(any(Notification.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
    }

    @Nested
    @DisplayName("Payment Events")
    class PaymentEvents {

        @Test
        @DisplayName("Should handle PaymentFailedEvent")
        void shouldHandlePaymentFailedEvent() {
            // Arrange
            PaymentFailedEvent event = new PaymentFailedEvent(
                    organisationId,
                    gymId,
                    BigDecimal.valueOf(99.99),
                    "Insufficient funds",
                    LocalDateTime.now().plusDays(3),
                    "inv-123",
                    null,
                    null);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

            // Act
            listener.handlePaymentFailedEvent(event);

            // Assert
            verify(notificationRepository).save(captor.capture());
            Notification saved = captor.getValue();
            assertThat(saved.getTitle()).contains("Payment");
            assertThat(saved.getGymId()).isEqualTo(gymId);
            assertThat(saved.getScope()).isEqualTo(Notification.NotificationScope.GYM);
            assertThat(saved.getPriority()).isEqualTo(NotificationPriority.CRITICAL);
            assertThat(saved.getEventType()).isEqualTo("PAYMENT_FAILED");
            verify(notificationDispatcher).dispatch(saved);
        }

        @Test
        @DisplayName("Should handle PaymentFailedEvent with null gymId as organisation-scoped")
        void shouldHandlePaymentFailedEventOrganisationScoped() {
            // Platform (subscription) failures have no single gym — see
            // StripeWebhookService.handleInvoicePaymentFailed.
            PaymentFailedEvent event = new PaymentFailedEvent(
                    organisationId,
                    null,
                    BigDecimal.valueOf(49.99),
                    "Card declined",
                    LocalDateTime.now().plusDays(3),
                    "inv-789",
                    null,
                    null);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

            listener.handlePaymentFailedEvent(event);

            verify(notificationRepository).save(captor.capture());
            Notification saved = captor.getValue();
            assertThat(saved.getScope()).isEqualTo(Notification.NotificationScope.ORGANISATION);
            assertThat(saved.getRelatedEntityType()).isEqualTo("ORGANISATION");
            assertThat(saved.getRelatedEntityId()).isEqualTo(organisationId);
        }

        @Test
        @DisplayName("Should handle PaymentSuccessEvent")
        void shouldHandlePaymentSuccessEvent() {
            // Arrange
            PaymentSuccessEvent event = new PaymentSuccessEvent(
                    organisationId,
                    gymId,
                    BigDecimal.valueOf(99.99),
                    "INV-001",
                    "https://example.com/invoice",
                    LocalDateTime.now(),
                    null,
                    null);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

            // Act
            listener.handlePaymentSuccessEvent(event);

            // Assert
            verify(notificationRepository).save(captor.capture());
            Notification saved = captor.getValue();
            assertThat(saved.getEventType()).isEqualTo("PAYMENT_SUCCESS");
            assertThat(saved.getScope()).isEqualTo(Notification.NotificationScope.GYM);
            assertThat(saved.getPriority()).isEqualTo(NotificationPriority.LOW);
            assertThat(saved.getGymId()).isEqualTo(gymId);
        }
    }

    @Nested
    @DisplayName("Subscription Events")
    class SubscriptionEvents {

        @Test
        @DisplayName("Should handle SubscriptionExpiringEvent")
        void shouldHandleSubscriptionExpiringEvent() {
            // Arrange
            SubscriptionExpiringEvent event = new SubscriptionExpiringEvent(
                    organisationId,
                    UUID.randomUUID(),
                    "Premium",
                    BigDecimal.valueOf(99.99),
                    LocalDateTime.now().plusDays(7),
                    7);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

            // Act
            listener.handleSubscriptionExpiringEvent(event);

            // Assert
            verify(notificationRepository).save(captor.capture());
            Notification saved = captor.getValue();
            assertThat(saved.getScope()).isEqualTo(Notification.NotificationScope.ORGANISATION);
            assertThat(saved.getGymId()).isNull();
            assertThat(saved.getRecipientRole()).isEqualTo(Notification.RecipientRole.OWNER);
            assertThat(saved.getEventType()).isEqualTo("SUBSCRIPTION_EXPIRING");
        }
    }

    @Nested
    @DisplayName("Member Events (NEW)")
    class MemberEvents {

        @Test
        @DisplayName("Should handle MemberJoinedEvent")
        void shouldHandleMemberJoinedEvent() {
            // Arrange
            MemberJoinedEvent event = new MemberJoinedEvent(
                    organisationId,
                    gymId,
                    UUID.randomUUID(),
                    "John Doe",
                    "john@example.com",
                    "Monthly");

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

            // Act
            listener.handleMemberJoinedEvent(event);

            // Assert
            verify(notificationRepository).save(captor.capture());
            Notification saved = captor.getValue();
            assertThat(saved.getGymId()).isEqualTo(gymId);
            assertThat(saved.getScope()).isEqualTo(Notification.NotificationScope.GYM);
            assertThat(saved.getRecipientRole()).isEqualTo(Notification.RecipientRole.GYM_MANAGER);
            assertThat(saved.getPriority()).isEqualTo(NotificationPriority.LOW);
            assertThat(saved.getEventType()).isEqualTo("MEMBER_JOINED");
        }
    }

    @Nested
    @DisplayName("Error Handling")
    class ErrorHandling {

        @Test
        @DisplayName("Should handle exception during event processing")
        void shouldHandleException() {
            // Arrange
            PaymentFailedEvent event = new PaymentFailedEvent(
                    organisationId,
                    gymId,
                    BigDecimal.valueOf(99.99),
                    null,
                    null,
                    null,
                    null,
                    null);

            when(notificationRepository.save(any())).thenThrow(RuntimeException.class);

            // Act & Assert - should not throw
            assertThatNoException().isThrownBy(() -> listener.handlePaymentFailedEvent(event));

            // Verify dispatcher was not called
            verify(notificationDispatcher, never()).dispatch(any());
        }

        @Test
        @DisplayName("Should handle database error gracefully")
        void shouldHandleDatabaseError() {
            // Arrange
            SubscriptionExpiringEvent event = new SubscriptionExpiringEvent(
                    organisationId,
                    UUID.randomUUID(),
                    "Premium",
                    null,
                    LocalDateTime.now().plusDays(7),
                    7);

            when(notificationRepository.save(any(Notification.class)))
                    .thenThrow(new RuntimeException("Database error"));

            // Act & Assert - should not throw
            assertThatNoException().isThrownBy(() -> listener.handleSubscriptionExpiringEvent(event));
        }
    }

    @Nested
    @DisplayName("Notification Properties")
    class NotificationProperties {

        @Test
        @DisplayName("Should set correct metadata for payment event")
        void shouldSetCorrectMetadataForPayment() {
            // Arrange
            PaymentFailedEvent event = new PaymentFailedEvent(
                    organisationId,
                    gymId,
                    BigDecimal.valueOf(99.99),
                    "Card declined",
                    LocalDateTime.now().plusDays(1),
                    "inv-456",
                    null,
                    null);

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

            // Act
            listener.handlePaymentFailedEvent(event);

            // Assert
            verify(notificationRepository).save(captor.capture());
            Notification saved = captor.getValue();
            assertThat(saved.getMetadata()).isNotEmpty();
            assertThat(saved.getRelatedEntityType()).isEqualTo("GYM");
        }

        @Test
        @DisplayName("Should set related entity ID correctly")
        void shouldSetRelatedEntityId() {
            // Arrange
            UUID memberId = UUID.randomUUID();
            MemberJoinedEvent event = new MemberJoinedEvent(
                    organisationId,
                    gymId,
                    memberId,
                    "Jane Doe",
                    null,
                    "Annual");

            ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);

            // Act
            listener.handleMemberJoinedEvent(event);

            // Assert
            verify(notificationRepository).save(captor.capture());
            Notification saved = captor.getValue();
            assertThat(saved.getRelatedEntityId()).isEqualTo(memberId);
            assertThat(saved.getRelatedEntityType()).isEqualTo("MEMBER");
        }
    }
}
