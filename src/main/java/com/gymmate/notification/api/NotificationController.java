package com.gymmate.notification.api;

import com.gymmate.notification.api.dto.NotificationResponse;
import com.gymmate.notification.application.NotificationService;
import com.gymmate.notification.domain.Notification;
import com.gymmate.shared.dto.ApiResponse;
import com.gymmate.shared.security.TenantAwareUserDetails;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * REST controller for notification management.
 * Provides CRUD and query endpoints for notifications.
 */
@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
@Slf4j
@Tag(name = "Notifications", description = "Notification management APIs")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * Get all notifications for the current organisation with pagination.
     */
    @GetMapping
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get notifications", description = "Get paginated list of notifications")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getNotifications(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID organisationId = userDetails.getOrganisationId();
        Pageable pageable = PageRequest.of(page, size);

        Page<Notification> notifications = notificationService.getNotifications(organisationId, pageable);
        Page<NotificationResponse> response = notifications.map(NotificationResponse::fromEntity);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get unread notifications.
     */
    @GetMapping("/unread")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get unread notifications", description = "Get paginated list of unread notifications")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUnreadNotifications(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID organisationId = userDetails.getOrganisationId();
        Pageable pageable = PageRequest.of(page, size);

        Page<Notification> notifications = notificationService.getUnreadNotifications(organisationId, pageable);
        Page<NotificationResponse> response = notifications.map(NotificationResponse::fromEntity);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get unread notification count.
     */
    @GetMapping("/unread-count")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get unread count", description = "Get count of unread notifications for badge display")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getUnreadCount(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        UUID organisationId = userDetails.getOrganisationId();
        long count = notificationService.getUnreadCount(organisationId);

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get a single notification by ID.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Get notification by ID")
    public ResponseEntity<ApiResponse<NotificationResponse>> getNotification(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails,
            @PathVariable UUID id) {
        Notification notification = notificationService.getById(id);

        // Security check: ensure notification belongs to current organisation
        UUID organisationId = userDetails.getOrganisationId();
        if (!notification.getOrganisationId().equals(organisationId)) {
            return ResponseEntity.status(403).body(ApiResponse.<NotificationResponse>error("Access denied"));
        }

        return ResponseEntity.ok(ApiResponse.success(NotificationResponse.fromEntity(notification)));
    }

    /**
     * Mark a notification as read.
     */
    @PatchMapping("/{id}/read")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Mark as read", description = "Mark a notification as read")
    public ResponseEntity<ApiResponse<NotificationResponse>> markAsRead(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails,
            @PathVariable UUID id) {
        Notification notification = notificationService.getById(id);

        // Security check
        UUID organisationId = userDetails.getOrganisationId();
        if (!notification.getOrganisationId().equals(organisationId)) {
            return ResponseEntity.status(403).body(ApiResponse.<NotificationResponse>error("Access denied"));
        }

        Notification updated = notificationService.markAsRead(id);
        return ResponseEntity.ok(ApiResponse.success(NotificationResponse.fromEntity(updated)));
    }

    /**
     * Mark all notifications as read.
     */
    @PostMapping("/mark-all-read")
    @PreAuthorize("hasAnyRole('OWNER', 'ADMIN')")
    @Operation(summary = "Mark all as read", description = "Mark all notifications as read for the organisation")
    public ResponseEntity<ApiResponse<String>> markAllAsRead(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails) {
        UUID organisationId = userDetails.getOrganisationId();
        notificationService.markAllAsRead(organisationId);

        return ResponseEntity.ok(ApiResponse.success("All notifications marked as read"));
    }

    // ============= Gym-Level Notification Endpoints (NEW) =============

    /**
     * Get all notifications for a specific gym with pagination.
     */
    @GetMapping("/gym/{gymId}")
    @PreAuthorize("hasAnyRole('GYM_MANAGER', 'STAFF', 'OWNER', 'ADMIN')")
    @Operation(summary = "Get gym notifications", description = "Get paginated list of notifications for a specific gym")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getGymNotifications(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails,
            @PathVariable UUID gymId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID organisationId = userDetails.getOrganisationId();
        // TODO: Verify user has access to this gym (check gym membership or role)

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getGymNotifications(gymId, pageable);
        Page<NotificationResponse> response = notifications.map(NotificationResponse::fromEntity);

        log.info("Retrieved {} gym notifications for gym: {}", notifications.getTotalElements(), gymId);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get unread notifications for a specific gym.
     */
    @GetMapping("/gym/{gymId}/unread")
    @PreAuthorize("hasAnyRole('GYM_MANAGER', 'STAFF', 'OWNER', 'ADMIN')")
    @Operation(summary = "Get unread gym notifications", description = "Get paginated list of unread notifications for a specific gym")
    public ResponseEntity<ApiResponse<Page<NotificationResponse>>> getUnreadGymNotifications(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails,
            @PathVariable UUID gymId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        UUID organisationId = userDetails.getOrganisationId();
        // TODO: Verify user has access to this gym

        Pageable pageable = PageRequest.of(page, size);
        Page<Notification> notifications = notificationService.getUnreadGymNotifications(gymId, pageable);
        Page<NotificationResponse> response = notifications.map(NotificationResponse::fromEntity);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Get unread notification count for a gym.
     */
    @GetMapping("/gym/{gymId}/unread-count")
    @PreAuthorize("hasAnyRole('GYM_MANAGER', 'STAFF', 'OWNER', 'ADMIN')")
    @Operation(summary = "Get gym unread count", description = "Get count of unread notifications for a gym (for badge display)")
    public ResponseEntity<ApiResponse<Map<String, Long>>> getGymUnreadCount(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails,
            @PathVariable UUID gymId) {

        UUID organisationId = userDetails.getOrganisationId();
        // TODO: Verify user has access to this gym

        long count = notificationService.getUnreadGymCount(gymId);

        Map<String, Long> response = new HashMap<>();
        response.put("count", count);

        log.info("Unread notification count for gym {}: {}", gymId, count);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * Mark all gym notifications as read.
     */
    @PostMapping("/gym/{gymId}/mark-all-read")
    @PreAuthorize("hasAnyRole('GYM_MANAGER', 'STAFF', 'OWNER', 'ADMIN')")
    @Operation(summary = "Mark all gym notifications as read", description = "Mark all notifications as read for a specific gym")
    public ResponseEntity<ApiResponse<String>> markAllGymNotificationsAsRead(
            @AuthenticationPrincipal TenantAwareUserDetails userDetails,
            @PathVariable UUID gymId) {

        UUID organisationId = userDetails.getOrganisationId();
        // TODO: Verify user has access to this gym

        notificationService.markAllGymNotificationsAsRead(gymId);
        log.info("Marked all notifications as read for gym: {}", gymId);

        return ResponseEntity.ok(ApiResponse.success("All gym notifications marked as read"));
    }

  /**
   * Test email notification endpoint- unauthenticated
   */
  @GetMapping("/test-email")
  public ResponseEntity<ApiResponse<String>> testEmailNotification() {

    notificationService.sendTestEmail("davidgodswill@gmail.com");
    return ResponseEntity.ok(ApiResponse.success("Test email sent successfully"));
  }
}
