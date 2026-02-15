package com.gymmate.unit.notification.application;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.gymmate.notification.application.AdminNotificationEventListener;
import com.gymmate.notification.application.NotificationDispatcher;
import com.gymmate.notification.domain.Notification;
import com.gymmate.notification.events.*;
import com.gymmate.notification.infrastructure.NotificationRepository;
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
    private ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

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
            PaymentFailedEvent event = PaymentFailedEvent.builder()
                    .organisationId(organisationId)
                    .gymId(gymId)
                    .amount(BigDecimal.valueOf(99.99))
                    .failureReason("Insufficient funds")
                    .nextRetryDate(LocalDateTime.now().plusDays(3))
                    .invoiceId("inv-123")
                    .build();

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
        @DisplayName("Should handle PaymentSuccessEvent")
        void shouldHandlePaymentSuccessEvent() {
            // Arrange
            PaymentSuccessEvent event = PaymentSuccessEvent.builder()
                    .organisationId(organisationId)
                    .gymId(gymId)
                    .amount(BigDecimal.valueOf(99.99))
                    .invoiceNumber("INV-001")
                    .invoiceUrl("https://example.com/invoice")
                    .periodEnd(LocalDateTime.now())
                    .build();

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
            SubscriptionExpiringEvent event = SubscriptionExpiringEvent.builder()
                    .organisationId(organisationId)
                    .subscriptionId(UUID.randomUUID())
                    .tierName("Premium")
                    .price(BigDecimal.valueOf(99.99))
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .daysUntilExpiry(7)
                    .build();

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
            MemberJoinedEvent event = MemberJoinedEvent.builder()
                    .organisationId(organisationId)
                    .gymId(gymId)
                    .memberId(UUID.randomUUID())
                    .memberName("John Doe")
                    .memberEmail("john@example.com")
                    .membershipPlan("Monthly")
                    .build();

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
            PaymentFailedEvent event = PaymentFailedEvent.builder()
                    .organisationId(organisationId)
                    .gymId(gymId)
                    .amount(BigDecimal.valueOf(99.99))
                    .build();

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
            SubscriptionExpiringEvent event = SubscriptionExpiringEvent.builder()
                    .organisationId(organisationId)
                    .subscriptionId(UUID.randomUUID())
                    .tierName("Premium")
                    .expiresAt(LocalDateTime.now().plusDays(7))
                    .daysUntilExpiry(7)
                    .build();

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
            PaymentFailedEvent event = PaymentFailedEvent.builder()
                    .organisationId(organisationId)
                    .gymId(gymId)
                    .amount(BigDecimal.valueOf(99.99))
                    .failureReason("Card declined")
                    .nextRetryDate(LocalDateTime.now().plusDays(1))
                    .invoiceId("inv-456")
                    .build();

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
            MemberJoinedEvent event = MemberJoinedEvent.builder()
                    .organisationId(organisationId)
                    .gymId(gymId)
                    .memberId(memberId)
                    .memberName("Jane Doe")
                    .membershipPlan("Annual")
                    .build();

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
