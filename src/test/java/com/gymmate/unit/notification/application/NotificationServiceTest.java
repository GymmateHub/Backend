package com.gymmate.unit.notification.application;

import com.gymmate.notification.application.EmailService;
import com.gymmate.notification.application.NotificationService;
import com.gymmate.notification.domain.Notification;
import com.gymmate.notification.events.NotificationPriority;
import com.gymmate.notification.infrastructure.NotificationRepository;
import com.gymmate.shared.exception.ResourceNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationService Unit Tests")
class NotificationServiceTest {

        @Mock
        private NotificationRepository notificationRepository;
        @Mock
        private com.gymmate.notification.infrastructure.SseEmitterRegistry sseEmitterRegistry;
        @Mock
        private com.fasterxml.jackson.databind.ObjectMapper objectMapper;
        @Mock
        private EmailService emailService;

        private NotificationService service;

        private UUID organisationId;
        private UUID gymId;
        private UUID notificationId;

        @BeforeEach
        void setUp() {
                service = new NotificationService(notificationRepository, sseEmitterRegistry, objectMapper, emailService);
                organisationId = UUID.randomUUID();
                gymId = UUID.randomUUID();
                notificationId = UUID.randomUUID();
        }

        @Nested
        @DisplayName("Broadcast Operations")
        class BroadcastOperations {
                @Test
                @DisplayName("Should create and broadcast notification")
                void shouldCreateAndBroadcast() {
                        // Arrange
                        String title = "Test Title";
                        String message = "Test Message";
                        NotificationPriority priority = NotificationPriority.HIGH;
                        String eventType = "TEST_EVENT";

                        when(notificationRepository.save(any(Notification.class)))
                                        .thenAnswer(i -> {
                                                Notification n = i.getArgument(0);
                                                n.setId(notificationId);
                                                return n;
                                        });

                        // Act
                        Notification result = service.createAndBroadcast(title, message, organisationId, priority,
                                        eventType, null);

                        // Assert
                        assertThat(result).isNotNull();
                        assertThat(result.getId()).isEqualTo(notificationId);
                        assertThat(result.getTitle()).isEqualTo(title);
                        assertThat(result.getOrganisationId()).isEqualTo(organisationId); // Verified via setter

                        verify(notificationRepository).save(any(Notification.class));
                        verify(sseEmitterRegistry).sendToOrganisation(eq(organisationId), any(Notification.class));
                }
        }

        @Nested
        @DisplayName("Organisation-Level Operations")
        class OrganisationLevelOperations {

                @Test
                @DisplayName("Should get paginated notifications")
                void shouldGetNotifications() {
                        // Arrange
                        Notification notification = buildNotification(organisationId, null);
                        Page<Notification> expectedPage = new PageImpl<>(List.of(notification));
                        when(notificationRepository.findByOrganisationId(organisationId, Pageable.unpaged()))
                                        .thenReturn(expectedPage);

                        // Act
                        Page<Notification> result = service.getNotifications(organisationId, Pageable.unpaged());

                        // Assert
                        assertThat(result).isEqualTo(expectedPage);
                        verify(notificationRepository).findByOrganisationId(organisationId, Pageable.unpaged());
                }

                @Test
                @DisplayName("Should get unread notifications")
                void shouldGetUnreadNotifications() {
                        // Arrange
                        Notification notification = buildNotification(organisationId, null);
                        Page<Notification> expectedPage = new PageImpl<>(List.of(notification));
                        when(notificationRepository.findUnreadByOrganisationId(organisationId, Pageable.unpaged()))
                                        .thenReturn(expectedPage);

                        // Act
                        Page<Notification> result = service.getUnreadNotifications(organisationId, Pageable.unpaged());

                        // Assert
                        assertThat(result).isEqualTo(expectedPage);
                        verify(notificationRepository).findUnreadByOrganisationId(organisationId, Pageable.unpaged());
                }

                @Test
                @DisplayName("Should get unread notification count")
                void shouldGetUnreadCount() {
                        // Arrange
                        long expectedCount = 5L;
                        when(notificationRepository.countUnreadByOrganisationId(organisationId))
                                        .thenReturn(expectedCount);

                        // Act
                        long result = service.getUnreadCount(organisationId);

                        // Assert
                        assertThat(result).isEqualTo(5L);
                        verify(notificationRepository).countUnreadByOrganisationId(organisationId);
                }

                @Test
                @DisplayName("Should get notification by ID")
                void shouldGetById() {
                        // Arrange
                        Notification notification = buildNotification(organisationId, null);
                        notification.setId(notificationId);
                        when(notificationRepository.findById(notificationId))
                                        .thenReturn(Optional.of(notification));

                        // Act
                        Notification result = service.getById(notificationId);

                        // Assert
                        assertThat(result).isEqualTo(notification);
                        verify(notificationRepository).findById(notificationId);
                }

                @Test
                @DisplayName("Should throw when notification not found")
                void shouldThrowNotFound() {
                        // Arrange
                        when(notificationRepository.findById(notificationId))
                                        .thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> service.getById(notificationId))
                                        .isInstanceOf(ResourceNotFoundException.class);
                }

                @Test
                @DisplayName("Should mark notification as read")
                void shouldMarkAsRead() {
                        // Arrange
                        Notification notification = buildNotification(organisationId, null);
                        notification.setId(notificationId);
                        when(notificationRepository.findById(notificationId))
                                        .thenReturn(Optional.of(notification));
                        when(notificationRepository.save(any(Notification.class)))
                                        .thenAnswer(i -> i.getArgument(0));

                        // Act
                        Notification result = service.markAsRead(notificationId);

                        // Assert
                        assertThat(result.isRead()).isTrue();
                        verify(notificationRepository).save(any(Notification.class));
                }

                @Test
                @DisplayName("Should mark all org notifications as read")
                void shouldMarkAllAsRead() {
                        // Arrange
                        Notification notif1 = buildNotification(organisationId, null);
                        Notification notif2 = buildNotification(organisationId, null);
                        Page<Notification> unreadPage = new PageImpl<>(List.of(notif1, notif2));

                        when(notificationRepository.findUnreadByOrganisationId(
                                        eq(organisationId), any(Pageable.class)))
                                        .thenReturn(unreadPage);
                        when(notificationRepository.save(any(Notification.class)))
                                        .thenAnswer(i -> i.getArgument(0));

                        // Act
                        service.markAllAsRead(organisationId);

                        // Assert
                        verify(notificationRepository, times(2)).save(any(Notification.class));
                }
        }

        @Nested
        @DisplayName("Gym-Level Operations (NEW)")
        class GymLevelOperations {

                @Test
                @DisplayName("Should get paginated gym notifications")
                void shouldGetGymNotifications() {
                        // Arrange
                        Notification notification = buildNotification(organisationId, gymId);
                        Page<Notification> expectedPage = new PageImpl<>(List.of(notification));
                        when(notificationRepository.findByGymId(gymId, Pageable.unpaged()))
                                        .thenReturn(expectedPage);

                        // Act
                        Page<Notification> result = service.getGymNotifications(gymId, Pageable.unpaged());

                        // Assert
                        assertThat(result).isEqualTo(expectedPage);
                        verify(notificationRepository).findByGymId(gymId, Pageable.unpaged());
                }

                @Test
                @DisplayName("Should get unread gym notifications")
                void shouldGetUnreadGymNotifications() {
                        // Arrange
                        Notification notification = buildNotification(organisationId, gymId);
                        Page<Notification> expectedPage = new PageImpl<>(List.of(notification));
                        when(notificationRepository.findUnreadByGymId(gymId, Pageable.unpaged()))
                                        .thenReturn(expectedPage);

                        // Act
                        Page<Notification> result = service.getUnreadGymNotifications(gymId, Pageable.unpaged());

                        // Assert
                        assertThat(result).isEqualTo(expectedPage);
                        verify(notificationRepository).findUnreadByGymId(gymId, Pageable.unpaged());
                }

                @Test
                @DisplayName("Should get gym unread notification count")
                void shouldGetGymUnreadCount() {
                        // Arrange
                        long expectedCount = 3L;
                        when(notificationRepository.countUnreadByGymId(gymId))
                                        .thenReturn(expectedCount);

                        // Act
                        long result = service.getUnreadGymCount(gymId);

                        // Assert
                        assertThat(result).isEqualTo(3L);
                        verify(notificationRepository).countUnreadByGymId(gymId);
                }

                @Test
                @DisplayName("Should mark all gym notifications as read")
                void shouldMarkAllGymNotificationsAsRead() {
                        // Arrange
                        Notification notif1 = buildNotification(organisationId, gymId);
                        Notification notif2 = buildNotification(organisationId, gymId);
                        Page<Notification> unreadPage = new PageImpl<>(List.of(notif1, notif2));

                        when(notificationRepository.findUnreadByGymId(
                                        eq(gymId), any(Pageable.class)))
                                        .thenReturn(unreadPage);
                        when(notificationRepository.save(any(Notification.class)))
                                        .thenAnswer(i -> i.getArgument(0));

                        // Act
                        service.markAllGymNotificationsAsRead(gymId);

                        // Assert
                        verify(notificationRepository, times(2)).save(any(Notification.class));
                }

                @Test
                @DisplayName("Should get recent gym notifications")
                void shouldGetRecentGymNotifications() {
                        // Arrange
                        Notification notification = buildNotification(organisationId, gymId);
                        List<Notification> expectedList = List.of(notification);
                        when(notificationRepository.findRecentByGymId(eq(gymId), any()))
                                        .thenReturn(expectedList);

                        // Act
                        List<Notification> result = service.getRecentGymNotifications(gymId, 7);

                        // Assert
                        assertThat(result).hasSize(1);
                        verify(notificationRepository).findRecentByGymId(eq(gymId), any());
                }
        }

        @Nested
        @DisplayName("Error Handling")
        class ErrorHandling {

                @Test
                @DisplayName("Should handle resource not found gracefully")
                void shouldHandleResourceNotFound() {
                        // Arrange
                        when(notificationRepository.findById(notificationId))
                                        .thenReturn(Optional.empty());

                        // Act & Assert
                        assertThatThrownBy(() -> service.getById(notificationId))
                                        .isInstanceOf(ResourceNotFoundException.class)
                                        .hasMessageContaining("Notification");
                }
        }

        // Helper methods
        private Notification buildNotification(UUID orgId, UUID gym) {
                return Notification.builder()
                                .title("Test Notification")
                                .message("Test message")
                                .priority(NotificationPriority.HIGH)
                                .eventType("TEST_EVENT")
                                .recipientRole(Notification.RecipientRole.OWNER)
                                .scope(gym != null ? Notification.NotificationScope.GYM
                                                : Notification.NotificationScope.ORGANISATION)
                                .gymId(gym)
                                .build();
        }
}
