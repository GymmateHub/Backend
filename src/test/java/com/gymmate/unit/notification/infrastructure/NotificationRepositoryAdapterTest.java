package com.gymmate.unit.notification.infrastructure;

import com.gymmate.notification.domain.Notification;
import com.gymmate.notification.events.NotificationPriority;
import com.gymmate.notification.infrastructure.NotificationJpaRepository;
import com.gymmate.notification.infrastructure.NotificationRepository;
import com.gymmate.notification.infrastructure.NotificationRepositoryAdapter;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationRepositoryAdapter Unit Tests")
class NotificationRepositoryAdapterTest {

    @Mock
    private NotificationJpaRepository jpaRepository;

    private NotificationRepository adapter;

    private UUID organisationId;
    private UUID gymId;
    private UUID notificationId;

    @BeforeEach
    void setUp() {
        adapter = new NotificationRepositoryAdapter(jpaRepository);
        organisationId = UUID.randomUUID();
        gymId = UUID.randomUUID();
        notificationId = UUID.randomUUID();
    }

    @Nested
    @DisplayName("Save Operations")
    class SaveOperations {

        @Test
        @DisplayName("Should save notification")
        void shouldSaveNotification() {
            // Arrange
            Notification notification = buildNotification(organisationId, null);
            when(jpaRepository.save(notification)).thenReturn(notification);

            // Act
            Notification result = adapter.save(notification);

            // Assert
            assertThat(result).isEqualTo(notification);
            verify(jpaRepository).save(notification);
        }

        @Test
        @DisplayName("Should save gym notification")
        void shouldSaveGymNotification() {
            // Arrange
            Notification notification = buildNotification(organisationId, gymId);
            when(jpaRepository.save(notification)).thenReturn(notification);

            // Act
            Notification result = adapter.save(notification);

            // Assert
            assertThat(result.getGymId()).isEqualTo(gymId);
            verify(jpaRepository).save(notification);
        }
    }

    @Nested
    @DisplayName("Find Operations")
    class FindOperations {

        @Test
        @DisplayName("Should find by ID")
        void shouldFindById() {
            // Arrange
            Notification notification = buildNotification(organisationId, null);
            when(jpaRepository.findById(notificationId)).thenReturn(Optional.of(notification));

            // Act
            Optional<Notification> result = adapter.findById(notificationId);

            // Assert
            assertThat(result).isPresent();
            verify(jpaRepository).findById(notificationId);
        }

        @Test
        @DisplayName("Should return empty when not found")
        void shouldReturnEmptyWhenNotFound() {
            // Arrange
            when(jpaRepository.findById(notificationId)).thenReturn(Optional.empty());

            // Act
            Optional<Notification> result = adapter.findById(notificationId);

            // Assert
            assertThat(result).isEmpty();
            verify(jpaRepository).findById(notificationId);
        }
    }

    @Nested
    @DisplayName("Organisation-Level Queries")
    class OrganisationLevelQueries {

        @Test
        @DisplayName("Should delegate findByOrganisationId to JPA")
        void shouldFindByOrganisationId() {
            // Arrange
            Notification notification = buildNotification(organisationId, null);
            Page<Notification> expectedPage = new PageImpl<>(List.of(notification));
            when(jpaRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId, Pageable.unpaged()))
                    .thenReturn(expectedPage);

            // Act
            Page<Notification> result = adapter.findByOrganisationId(organisationId, Pageable.unpaged());

            // Assert
            assertThat(result).isEqualTo(expectedPage);
            verify(jpaRepository).findByOrganisationIdOrderByCreatedAtDesc(organisationId, Pageable.unpaged());
        }

        @Test
        @DisplayName("Should delegate findUnreadByOrganisationId to JPA")
        void shouldFindUnreadByOrganisationId() {
            // Arrange
            Notification notification = buildNotification(organisationId, null);
            Page<Notification> expectedPage = new PageImpl<>(List.of(notification));
            when(jpaRepository.findUnreadByOrganisationId(organisationId, Pageable.unpaged()))
                    .thenReturn(expectedPage);

            // Act
            Page<Notification> result = adapter.findUnreadByOrganisationId(organisationId, Pageable.unpaged());

            // Assert
            assertThat(result).isEqualTo(expectedPage);
            verify(jpaRepository).findUnreadByOrganisationId(organisationId, Pageable.unpaged());
        }

        @Test
        @DisplayName("Should delegate countUnreadByOrganisationId to JPA")
        void shouldCountUnreadByOrganisationId() {
            // Arrange
            when(jpaRepository.countUnreadByOrganisationId(organisationId)).thenReturn(5L);

            // Act
            long result = adapter.countUnreadByOrganisationId(organisationId);

            // Assert
            assertThat(result).isEqualTo(5L);
            verify(jpaRepository).countUnreadByOrganisationId(organisationId);
        }

        @Test
        @DisplayName("Should delegate findRecentByOrganisationId to JPA")
        void shouldFindRecentByOrganisationId() {
            // Arrange
            Notification notification = buildNotification(organisationId, null);
            List<Notification> expectedList = List.of(notification);
            when(jpaRepository.findRecentByOrganisationId(eq(organisationId), any()))
                    .thenReturn(expectedList);

            // Act
            List<Notification> result = adapter.findRecentByOrganisationId(organisationId, LocalDateTime.now());

            // Assert
            assertThat(result).hasSize(1);
            verify(jpaRepository).findRecentByOrganisationId(eq(organisationId), any());
        }
    }

    @Nested
    @DisplayName("Gym-Level Queries (NEW)")
    class GymLevelQueries {

        @Test
        @DisplayName("Should delegate findByGymId to JPA")
        void shouldFindByGymId() {
            // Arrange
            Notification notification = buildNotification(organisationId, gymId);
            Page<Notification> expectedPage = new PageImpl<>(List.of(notification));
            when(jpaRepository.findByGymIdOrderByCreatedAtDesc(gymId, Pageable.unpaged()))
                    .thenReturn(expectedPage);

            // Act
            Page<Notification> result = adapter.findByGymId(gymId, Pageable.unpaged());

            // Assert
            assertThat(result).isEqualTo(expectedPage);
            verify(jpaRepository).findByGymIdOrderByCreatedAtDesc(gymId, Pageable.unpaged());
        }

        @Test
        @DisplayName("Should delegate findUnreadByGymId to JPA")
        void shouldFindUnreadByGymId() {
            // Arrange
            Notification notification = buildNotification(organisationId, gymId);
            Page<Notification> expectedPage = new PageImpl<>(List.of(notification));
            when(jpaRepository.findUnreadByGymId(gymId, Pageable.unpaged()))
                    .thenReturn(expectedPage);

            // Act
            Page<Notification> result = adapter.findUnreadByGymId(gymId, Pageable.unpaged());

            // Assert
            assertThat(result).isEqualTo(expectedPage);
            verify(jpaRepository).findUnreadByGymId(gymId, Pageable.unpaged());
        }

        @Test
        @DisplayName("Should delegate countUnreadByGymId to JPA")
        void shouldCountUnreadByGymId() {
            // Arrange
            when(jpaRepository.countUnreadByGymId(gymId)).thenReturn(3L);

            // Act
            long result = adapter.countUnreadByGymId(gymId);

            // Assert
            assertThat(result).isEqualTo(3L);
            verify(jpaRepository).countUnreadByGymId(gymId);
        }

        @Test
        @DisplayName("Should delegate findRecentByGymId to JPA")
        void shouldFindRecentByGymId() {
            // Arrange
            Notification notification = buildNotification(organisationId, gymId);
            List<Notification> expectedList = List.of(notification);
            when(jpaRepository.findRecentByGymId(eq(gymId), any()))
                    .thenReturn(expectedList);

            // Act
            List<Notification> result = adapter.findRecentByGymId(gymId, LocalDateTime.now());

            // Assert
            assertThat(result).hasSize(1);
            verify(jpaRepository).findRecentByGymId(eq(gymId), any());
        }
    }

    @Nested
    @DisplayName("Delete Operations")
    class DeleteOperations {

        @Test
        @DisplayName("Should delete notification")
        void shouldDeleteNotification() {
            // Arrange
            Notification notification = buildNotification(organisationId, null);

            // Act
            adapter.delete(notification);

            // Assert
            verify(jpaRepository).delete(notification);
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

