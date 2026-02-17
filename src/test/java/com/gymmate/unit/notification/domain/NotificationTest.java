package com.gymmate.unit.notification.domain;

import com.gymmate.notification.domain.Notification;
import com.gymmate.notification.events.NotificationPriority;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;

@DisplayName("Notification Domain Entity Tests")
class NotificationTest {

    private Notification notification;
    private UUID organisationId;
    private UUID gymId;

    @BeforeEach
    void setUp() {
        organisationId = UUID.randomUUID();
        gymId = UUID.randomUUID();
        notification = Notification.builder()
                .title("Test Notification")
                .message("Test message")
                .priority(NotificationPriority.HIGH)
                .eventType("TEST_EVENT")
                .recipientRole(Notification.RecipientRole.OWNER)
                .scope(Notification.NotificationScope.ORGANISATION)
                .build();
    }

    @Nested
    @DisplayName("Organisation-Level Notifications")
    class OrganisationLevelTests {

        @Test
        @DisplayName("Should create organisation-level notification")
        void shouldCreateOrganisationLevelNotification() {
            // Assert
            assertThat(notification.isOrganisationScoped()).isTrue();
            assertThat(notification.isGymScoped()).isFalse();
            assertThat(notification.getGymId()).isNull();
            assertThat(notification.getScope()).isEqualTo(Notification.NotificationScope.ORGANISATION);
        }

        @Test
        @DisplayName("Should mark organisation notification as read")
        void shouldMarkOrganisationNotificationAsRead() {
            // Initial state
            assertThat(notification.isRead()).isFalse();
            assertThat(notification.getReadAt()).isNull();

            // Act
            notification.markAsRead();

            // Assert
            assertThat(notification.isRead()).isTrue();
            assertThat(notification.getReadAt()).isNotNull();
        }

        @Test
        @DisplayName("Should detect organisation scope correctly")
        void shouldDetectOrganisationScope() {
            // Assert
            assertThat(notification.isOrganisationScoped()).isTrue();
            assertThat(notification.getScope()).isEqualTo(Notification.NotificationScope.ORGANISATION);
        }
    }

    @Nested
    @DisplayName("Gym-Level Notifications (NEW)")
    class GymLevelTests {

        @Test
        @DisplayName("Should create gym-level notification")
        void shouldCreateGymLevelNotification() {
            // Arrange
            Notification gymNotification = Notification.builder()
                    .gymId(gymId)
                    .title("Gym Payment Failed")
                    .message("Payment failed for gym")
                    .priority(NotificationPriority.CRITICAL)
                    .eventType("PAYMENT_FAILED")
                    .scope(Notification.NotificationScope.GYM)
                    .recipientRole(Notification.RecipientRole.GYM_MANAGER)
                    .build();

            // Assert
            assertThat(gymNotification.isGymScoped()).isTrue();
            assertThat(gymNotification.isOrganisationScoped()).isFalse();
            assertThat(gymNotification.getGymId()).isEqualTo(gymId);
            assertThat(gymNotification.getScope()).isEqualTo(Notification.NotificationScope.GYM);
            assertThat(gymNotification.getRecipientRole()).isEqualTo(Notification.RecipientRole.GYM_MANAGER);
        }

        @Test
        @DisplayName("Should detect gym scope correctly when gymId is set")
        void shouldDetectGymScopeWithGymId() {
            // Arrange
            Notification gymNotif = Notification.builder()
                    .gymId(gymId)
                    .scope(Notification.NotificationScope.GYM)
                    .title("Test")
                    .message("Test")
                    .build();

            // Assert
            assertThat(gymNotif.isGymScoped()).isTrue();
        }

        @Test
        @DisplayName("Should not be gym scoped if gymId is null")
        void shouldNotBeGymScopedIfGymIdNull() {
            // Arrange
            Notification notif = Notification.builder()
                    .gymId(null)
                    .scope(Notification.NotificationScope.GYM)
                    .title("Test")
                    .message("Test")
                    .build();

            // Assert
            assertThat(notif.isGymScoped()).isFalse();
        }

        @Test
        @DisplayName("Should mark gym notification as read")
        void shouldMarkGymNotificationAsRead() {
            // Arrange
            Notification gymNotif = Notification.builder()
                    .gymId(gymId)
                    .scope(Notification.NotificationScope.GYM)
                    .title("Test")
                    .message("Test")
                    .build();

            // Initial state
            assertThat(gymNotif.isRead()).isFalse();

            // Act
            gymNotif.markAsRead();

            // Assert
            assertThat(gymNotif.isRead()).isTrue();
            assertThat(gymNotif.getReadAt()).isNotNull();
        }
    }

    @Nested
    @DisplayName("Notification Delivery")
    class DeliveryTests {

        @Test
        @DisplayName("Should mark notification as delivered via SSE")
        void shouldMarkAsDeliveredViaSSE() {
            // Act
            notification.markAsDelivered(Notification.DeliveryChannel.SSE);

            // Assert
            assertThat(notification.isDelivered()).isTrue();
            assertThat(notification.getDeliveredVia()).isEqualTo(Notification.DeliveryChannel.SSE);
            assertThat(notification.getDeliveredAt()).isNotNull();
        }

        @Test
        @DisplayName("Should mark notification as delivered via EMAIL")
        void shouldMarkAsDeliveredViaEmail() {
            // Act
            notification.markAsDelivered(Notification.DeliveryChannel.EMAIL);

            // Assert
            assertThat(notification.isDelivered()).isTrue();
            assertThat(notification.getDeliveredVia()).isEqualTo(Notification.DeliveryChannel.EMAIL);
        }

        @Test
        @DisplayName("Should mark notification as delivered via both channels")
        void shouldMarkAsDeliveredViaBothChannels() {
            // Act
            notification.markAsDelivered(Notification.DeliveryChannel.BOTH);

            // Assert
            assertThat(notification.isDelivered()).isTrue();
            assertThat(notification.getDeliveredVia()).isEqualTo(Notification.DeliveryChannel.BOTH);
        }

        @Test
        @DisplayName("Should not be delivered initially")
        void shouldNotBeDeliveredInitially() {
            // Assert
            assertThat(notification.isDelivered()).isFalse();
            assertThat(notification.getDeliveredVia()).isNull();
            assertThat(notification.getDeliveredAt()).isNull();
        }
    }

    @Nested
    @DisplayName("Enum Tests")
    class EnumTests {

        @Test
        @DisplayName("Should have all RecipientRole values")
        void shouldHaveAllRecipientRoles() {
            // Assert
            assertThat(Notification.RecipientRole.values())
                    .contains(
                            Notification.RecipientRole.OWNER,
                            Notification.RecipientRole.ADMIN,
                            Notification.RecipientRole.STAFF,
                            Notification.RecipientRole.GYM_MANAGER,
                            Notification.RecipientRole.SUPER_ADMIN
                    );
        }

        @Test
        @DisplayName("Should have all NotificationScope values")
        void shouldHaveAllScopes() {
            // Assert
            assertThat(Notification.NotificationScope.values())
                    .contains(
                            Notification.NotificationScope.ORGANISATION,
                            Notification.NotificationScope.GYM
                    );
        }

        @Test
        @DisplayName("Should have all DeliveryChannel values")
        void shouldHaveAllDeliveryChannels() {
            // Assert
            assertThat(Notification.DeliveryChannel.values())
                    .contains(
                            Notification.DeliveryChannel.EMAIL,
                            Notification.DeliveryChannel.SSE,
                            Notification.DeliveryChannel.BOTH
                    );
        }
    }

    @Nested
    @DisplayName("Builder Tests")
    class BuilderTests {

        @Test
        @DisplayName("Should create notification with all fields")
        void shouldCreateNotificationWithAllFields() {
            // Arrange
            UUID notificationId = UUID.randomUUID();
            LocalDateTime now = LocalDateTime.now();

            // Act
            Notification notif = Notification.builder()
                    .title("Full Test")
                    .message("Full message")
                    .priority(NotificationPriority.CRITICAL)
                    .eventType("TEST_EVENT")
                    .metadata("{}")
                    .recipientRole(Notification.RecipientRole.OWNER)
                    .scope(Notification.NotificationScope.GYM)
                    .gymId(gymId)
                    .relatedEntityId(UUID.randomUUID())
                    .relatedEntityType("GYM")
                    .deliveredVia(Notification.DeliveryChannel.SSE)
                    .build();

            // Assert
            assertThat(notif.getTitle()).isEqualTo("Full Test");
            assertThat(notif.getMessage()).isEqualTo("Full message");
            assertThat(notif.getPriority()).isEqualTo(NotificationPriority.CRITICAL);
            assertThat(notif.getEventType()).isEqualTo("TEST_EVENT");
            assertThat(notif.getGymId()).isEqualTo(gymId);
            assertThat(notif.getScope()).isEqualTo(Notification.NotificationScope.GYM);
        }

        @Test
        @DisplayName("Should have default scope ORGANISATION")
        void shouldHaveDefaultOrganisationScope() {
            // Arrange
            Notification notif = Notification.builder()
                    .title("Test")
                    .message("Test")
                    .build();

            // Assert
            assertThat(notif.getScope()).isEqualTo(Notification.NotificationScope.ORGANISATION);
        }

        @Test
        @DisplayName("Should have default priority MEDIUM")
        void shouldHaveDefaultMediumPriority() {
            // Arrange
            Notification notif = Notification.builder()
                    .title("Test")
                    .message("Test")
                    .build();

            // Assert
            assertThat(notif.getPriority()).isEqualTo(NotificationPriority.MEDIUM);
        }
    }
}

