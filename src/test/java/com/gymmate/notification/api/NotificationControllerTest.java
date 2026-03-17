package com.gymmate.notification.api;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;

import com.gymmate.gym.domain.Gym;
import com.gymmate.gym.infrastructure.GymRepository;
import com.gymmate.notification.application.NotificationService;
import com.gymmate.notification.domain.Notification;
import com.gymmate.notification.events.NotificationPriority;
import com.gymmate.shared.exception.GlobalExceptionHandler;
import com.gymmate.shared.security.TenantAwareUserDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.web.PageableHandlerMethodArgumentResolver;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.method.annotation.AuthenticationPrincipalArgumentResolver;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("NotificationController Unit Tests")
class NotificationControllerTest {

    private MockMvc mockMvc;

    @Mock
    private NotificationService notificationService;

    @Mock
    private GymRepository gymRepository;

    @InjectMocks
    private NotificationController notificationController;

    private ObjectMapper objectMapper;

    private UUID organisationId;
    private UUID gymId;
    private UUID notificationId;
    private TenantAwareUserDetails ownerUser;
    private TenantAwareUserDetails staffUser;
    private TenantAwareUserDetails gymManagerUser;

    @BeforeEach
    void setUp() {
        objectMapper = new ObjectMapper();
        objectMapper.registerModule(new JavaTimeModule());

        mockMvc = MockMvcBuilders.standaloneSetup(notificationController)
                .setControllerAdvice(new GlobalExceptionHandler())
                .setCustomArgumentResolvers(
                        new AuthenticationPrincipalArgumentResolver(),
                        new PageableHandlerMethodArgumentResolver())
                .build();

        organisationId = UUID.randomUUID();
        gymId = UUID.randomUUID();
        notificationId = UUID.randomUUID();

        ownerUser = new TenantAwareUserDetails(
                UUID.randomUUID(), organisationId, "owner@test.com", "password", "OWNER", true, true);

        staffUser = new TenantAwareUserDetails(
                UUID.randomUUID(), organisationId, "staff@test.com", "password", "STAFF", true, true);

        gymManagerUser = new TenantAwareUserDetails(
                UUID.randomUUID(), organisationId, "manager@test.com", "password", "GYM_MANAGER", true, true);
    }

    /** Sets the given user as the authenticated principal in the SecurityContext. */
    private void authenticateAs(TenantAwareUserDetails user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities()));
    }

    @Nested
    @DisplayName("Organisation-Level Endpoints")
    class OrganisationEndpoints {

        @Test
        @DisplayName("Should GET organisation notifications")
        void shouldGetOrganisationNotifications() throws Exception {
            authenticateAs(ownerUser);

            Notification notification = buildNotification(organisationId, null);
            Page<Notification> page = new PageImpl<>(List.of(notification), PageRequest.of(0, 20), 1);
            when(notificationService.getNotifications(eq(organisationId), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/notifications")
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
            authenticateAs(ownerUser);

            Notification notification = buildNotification(organisationId, null);
            Page<Notification> page = new PageImpl<>(List.of(notification), PageRequest.of(0, 20), 1);
            when(notificationService.getUnreadNotifications(eq(organisationId), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/notifications/unread")
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
            authenticateAs(ownerUser);

            when(notificationService.getUnreadCount(organisationId)).thenReturn(5L);

            mockMvc.perform(get("/api/notifications/unread-count")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.count").value(5));
        }

        @Test
        @DisplayName("Should mark all as read")
        void shouldMarkAllAsRead() throws Exception {
            authenticateAs(ownerUser);

            mockMvc.perform(post("/api/notifications/mark-all-read")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(notificationService).markAllAsRead(organisationId);
        }
    }

    @Nested
    @DisplayName("Gym-Level Endpoints")
    class GymEndpoints {

        @Test
        @DisplayName("Should GET gym notifications")
        void shouldGetGymNotifications() throws Exception {
            authenticateAs(gymManagerUser);

            Gym gym = new Gym("Test Gym", "Test", "test@gym.com", "1234567890", organisationId);
            gym.setId(gymId);
            when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));

            Notification notification = buildNotification(organisationId, gymId);
            Page<Notification> page = new PageImpl<>(List.of(notification), PageRequest.of(0, 20), 1);
            when(notificationService.getGymNotifications(eq(gymId), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/notifications/gym/{gymId}", gymId)
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
            authenticateAs(staffUser);

            Gym gym = new Gym("Test Gym", "Test", "test@gym.com", "1234567890", organisationId);
            gym.setId(gymId);
            when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));

            when(notificationService.getUnreadGymCount(gymId)).thenReturn(3L);

            mockMvc.perform(get("/api/notifications/gym/{gymId}/unread-count", gymId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true))
                    .andExpect(jsonPath("$.data.count").value(3));

            verify(notificationService).getUnreadGymCount(gymId);
        }

        @Test
        @DisplayName("Should mark gym notifications as read")
        void shouldMarkGymAsRead() throws Exception {
            authenticateAs(gymManagerUser);

            Gym gym = new Gym("Test Gym", "Test", "test@gym.com", "1234567890", organisationId);
            gym.setId(gymId);
            when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));

            mockMvc.perform(post("/api/notifications/gym/{gymId}/mark-all-read", gymId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));

            verify(notificationService).markAllGymNotificationsAsRead(gymId);
        }

        @Test
        @DisplayName("Should GET unread gym notifications")
        void shouldGetUnreadGymNotifications() throws Exception {
            authenticateAs(staffUser);

            Gym gym = new Gym("Test Gym", "Test", "test@gym.com", "1234567890", organisationId);
            gym.setId(gymId);
            when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));

            Notification notification = buildNotification(organisationId, gymId);
            notification.setReadAt(null);
            Page<Notification> page = new PageImpl<>(List.of(notification), PageRequest.of(0, 20), 1);
            when(notificationService.getUnreadGymNotifications(eq(gymId), any()))
                    .thenReturn(page);

            mockMvc.perform(get("/api/notifications/gym/{gymId}/unread", gymId)
                    .param("page", "0")
                    .param("size", "20")
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.success").value(true));
        }
    }

    @Nested
    @DisplayName("Gym Access Verification")
    class GymAccessTests {

        @Test
        @DisplayName("Should deny access when gym belongs to different organisation")
        void shouldDenyAccessWhenGymBelongsToDifferentOrg() throws Exception {
            authenticateAs(ownerUser);

            UUID otherOrgId = UUID.randomUUID();
            Gym gym = new Gym("Other Gym", "Other", "other@gym.com", "0987654321", otherOrgId);
            gym.setId(gymId);
            when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));

            mockMvc.perform(get("/api/notifications/gym/{gymId}/unread-count", gymId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        @DisplayName("Should return 404 when gym not found")
        void shouldReturn404WhenGymNotFound() throws Exception {
            authenticateAs(ownerUser);

            when(gymRepository.findById(gymId)).thenReturn(Optional.empty());

            mockMvc.perform(get("/api/notifications/gym/{gymId}/unread-count", gymId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("Should allow access when gym belongs to same organisation")
        void shouldAllowAccessWhenGymBelongsToSameOrg() throws Exception {
            authenticateAs(ownerUser);

            Gym gym = new Gym("Test Gym", "Test", "test@gym.com", "1234567890", organisationId);
            gym.setId(gymId);
            when(gymRepository.findById(gymId)).thenReturn(Optional.of(gym));
            when(notificationService.getUnreadGymCount(gymId)).thenReturn(0L);

            mockMvc.perform(get("/api/notifications/gym/{gymId}/unread-count", gymId)
                    .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk());
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
