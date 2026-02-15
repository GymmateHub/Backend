package com.gymmate.notification.api;

import com.fasterxml.jackson.databind.ObjectMapper;

import com.gymmate.notification.application.NotificationService;
import com.gymmate.notification.domain.Notification;
import com.gymmate.notification.events.NotificationPriority;
import com.gymmate.shared.security.CustomUserDetailsService;
import com.gymmate.shared.security.TenantAwareUserDetails;
import com.gymmate.shared.security.service.JwtService;
import com.gymmate.subscription.application.RateLimitService;
import com.gymmate.subscription.application.RateLimitStatus;
import com.gymmate.shared.security.config.SecurityConfig;
import com.gymmate.user.infrastructure.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import org.springframework.http.MediaType;

import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(NotificationController.class)
@Import(SecurityConfig.class)
@ActiveProfiles("test")
@DisplayName("NotificationController Unit Tests")
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CustomUserDetailsService customUserDetailsService;

    @MockBean
    private UserRepository userRepository;

    @MockBean
    private RateLimitService rateLimitService;

    @Autowired
    private ObjectMapper objectMapper;

    private UUID organisationId;
    private UUID gymId;
    private UUID notificationId;
    private TenantAwareUserDetails ownerUser;
    private TenantAwareUserDetails staffUser;
    private TenantAwareUserDetails gymManagerUser;

    @BeforeEach
    void setUp() {
        organisationId = UUID.randomUUID();
        gymId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        ownerUser = new TenantAwareUserDetails(
                UUID.randomUUID(), organisationId, "owner@test.com", "password", "OWNER", true, true);

        staffUser = new TenantAwareUserDetails(
                UUID.randomUUID(), organisationId, "staff@test.com", "password", "STAFF", true, true);

        gymManagerUser = new TenantAwareUserDetails(
                UUID.randomUUID(), organisationId, "manager@test.com", "password", "GYM_MANAGER", true, true);

        // Allow all requests through the rate limiter
        when(rateLimitService.checkRateLimit(any(), any(), any(), any())).thenReturn(true);
        when(rateLimitService.getRateLimitStatus(any())).thenReturn(
                RateLimitStatus.builder()
                        .hourlyLimit(1000).hourlyUsed(0).hourlyRemaining(1000)
                        .burstLimit(100).burstUsed(0).burstRemaining(100)
                        .isBlocked(false).tierName("Test")
                        .build());
    }

    @Nested
    @DisplayName("Organisation-Level Endpoints")
    class OrganisationEndpoints {

        @Test
        @DisplayName("Should GET organisation notifications")
        void shouldGetOrganisationNotifications() throws Exception {
            // Arrange
            Notification notification = buildNotification(organisationId, null);
            Page<Notification> page = new PageImpl<>(List.of(notification));
            when(notificationService.getNotifications(eq(organisationId), any()))
                    .thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/notifications")
                    .with(user(ownerUser))
                    .param("page", "0")
                    .param("size", "20")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.totalElements").value(1));

            verify(notificationService).getNotifications(eq(organisationId), any());
        }

        @Test
        @DisplayName("Should GET unread notifications")
        void shouldGetUnreadNotifications() throws Exception {
            // Arrange
            Notification notification = buildNotification(organisationId, null);
            Page<Notification> page = new PageImpl<>(List.of(notification));
            when(notificationService.getUnreadNotifications(eq(organisationId), any()))
                    .thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/notifications/unread")
                    .with(user(ownerUser))
                    .param("page", "0")
                    .param("size", "20")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(notificationService).getUnreadNotifications(eq(organisationId), any());
        }

        @Test
        @DisplayName("Should GET unread count")
        void shouldGetUnreadCount() throws Exception {
            // Arrange
            when(notificationService.getUnreadCount(organisationId)).thenReturn(5L);

            // Act & Assert
            mockMvc.perform(get("/api/notifications/unread-count")
                    .with(user(ownerUser))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.count").value(5));
        }

        @Test
        @DisplayName("Should mark all as read")
        void shouldMarkAllAsRead() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/notifications/mark-all-read")
                    .with(user(ownerUser))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(notificationService).markAllAsRead(organisationId);
        }
    }

    @Nested
    @DisplayName("Gym-Level Endpoints (NEW)")
    class GymEndpoints {

        @Test
        @DisplayName("Should GET gym notifications")
        void shouldGetGymNotifications() throws Exception {
            // Arrange
            Notification notification = buildNotification(organisationId, gymId);
            Page<Notification> page = new PageImpl<>(List.of(notification));
            when(notificationService.getGymNotifications(eq(gymId), any()))
                    .thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/notifications/gym/{gymId}", gymId)
                    .with(user(gymManagerUser))
                    .param("page", "0")
                    .param("size", "20")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.content[0].gymId").value(gymId.toString()))
                    .andExpect(jsonPath("$.data.content[0].scope").value("GYM"));

            verify(notificationService).getGymNotifications(eq(gymId), any());
        }

        @Test
        @DisplayName("Should GET gym unread count")
        void shouldGetGymUnreadCount() throws Exception {
            // Arrange
            when(notificationService.getUnreadGymCount(gymId)).thenReturn(3L);

            // Act & Assert
            mockMvc.perform(get("/api/notifications/gym/{gymId}/unread-count", gymId)
                    .with(user(staffUser))
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.count").value(3));

            verify(notificationService).getUnreadGymCount(gymId);
        }

        @Test
        @DisplayName("Should mark gym notifications as read")
        void shouldMarkGymAsRead() throws Exception {
            // Act & Assert
            mockMvc.perform(post("/api/notifications/gym/{gymId}/mark-all-read", gymId)
                    .with(user(gymManagerUser))
                    .with(csrf())
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(notificationService).markAllGymNotificationsAsRead(gymId);
        }

        @Test
        @DisplayName("Should GET unread gym notifications")
        void shouldGetUnreadGymNotifications() throws Exception {
            // Arrange
            Notification notification = buildNotification(organisationId, gymId);
            notification.setReadAt(null);
            Page<Notification> page = new PageImpl<>(List.of(notification));
            when(notificationService.getUnreadGymNotifications(eq(gymId), any()))
                    .thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/notifications/gym/{gymId}/unread", gymId)
                    .with(user(staffUser))
                    .param("page", "0")
                    .param("size", "20")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("Security & Authorization")
    class SecurityTests {

        @Test
        @DisplayName("Should deny access without authentication")
        void shouldDenyUnauthorized() throws Exception {
            // In @WebMvcTest context with default security filters, unauthenticated
            // requests receive 403 (Forbidden) rather than 401 (Unauthorized)
            mockMvc.perform(get("/api/notifications"))
                    .andExpect(status().isForbidden());
        }

        @Test
        @DisplayName("Should allow OWNER access to org endpoints")
        void shouldAllowOwnerAccess() throws Exception {
            // Arrange
            Page<Notification> page = new PageImpl<>(List.of());
            when(notificationService.getNotifications(any(), any())).thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/notifications")
                    .with(user(ownerUser)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow GYM_MANAGER access to gym endpoints")
        void shouldAllowGymManagerAccess() throws Exception {
            // Arrange
            Page<Notification> page = new PageImpl<>(List.of());
            when(notificationService.getGymNotifications(any(), any())).thenReturn(page);

            // Act & Assert
            mockMvc.perform(get("/api/notifications/gym/{gymId}", gymId)
                    .with(user(gymManagerUser)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should allow STAFF access to gym endpoints")
        void shouldAllowStaffAccess() throws Exception {
            // Arrange
            when(notificationService.getUnreadGymCount(any())).thenReturn(0L);

            // Act & Assert
            mockMvc.perform(get("/api/notifications/gym/{gymId}/unread-count", gymId)
                    .with(user(staffUser)))
                    .andExpect(status().isOk());
        }

        @Test
        @DisplayName("Should deny STAFF access to organisation endpoints")
        void shouldDenyStaffOrgAccess() throws Exception {
            mockMvc.perform(get("/api/notifications")
                    .with(user(staffUser)))
                    .andExpect(status().isForbidden());
        }
    }

    // Helper methods
    private Notification buildNotification(UUID orgId, UUID gym) {
        Notification notification = Notification.builder()
                .title("Test Notification")
                .message("Test message")
                .priority(NotificationPriority.HIGH)
                .eventType("TEST_EVENT")
                .recipientRole(Notification.RecipientRole.OWNER)
                .scope(gym != null ? Notification.NotificationScope.GYM
                        : Notification.NotificationScope.ORGANISATION)
                .gymId(gym)
                .build();
        notification.setId(notificationId);
        return notification;
    }
}
