package com.gymmate.notification.application;

import com.gymmate.notification.domain.Notification;
import com.gymmate.notification.infrastructure.NotificationRepository;
import com.gymmate.shared.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * Application service for notification management.
 * Provides CRUD operations and queries for notifications.
 */
@Service
@Slf4j
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;

    /**
     * Get all notifications for an organisation with pagination.
     */
    public Page<Notification> getNotifications(UUID organisationId, Pageable pageable) {
        log.debug("Fetching notifications for organisation: {}", organisationId);
        return notificationRepository.findByOrganisationId(organisationId, pageable);
    }

    /**
     * Get unread notifications for an organisation.
     */
    public Page<Notification> getUnreadNotifications(UUID organisationId, Pageable pageable) {
        log.debug("Fetching unread notifications for organisation: {}", organisationId);
        return notificationRepository.findUnreadByOrganisationId(organisationId, pageable);
    }

    /**
     * Get count of unread notifications.
     */
    public long getUnreadCount(UUID organisationId) {
        return notificationRepository.countUnreadByOrganisationId(organisationId);
    }

    /**
     * Get recent notifications (last 7 days).
     */
    public List<Notification> getRecentNotifications(UUID organisationId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return notificationRepository.findRecentByOrganisationId(organisationId, since);
    }

    /**
     * Get notification by ID.
     */
    public Notification getById(UUID notificationId) {
        return notificationRepository.findById(notificationId)
                .orElseThrow(() -> new ResourceNotFoundException("Notification", notificationId.toString()));
    }

    /**
     * Mark a notification as read.
     */
    @Transactional
    public Notification markAsRead(UUID notificationId) {
        Notification notification = getById(notificationId);
        notification.markAsRead();
        Notification saved = notificationRepository.save(notification);
        log.info("Marked notification {} as read", notificationId);
        return saved;
    }

    /**
     * Mark all notifications as read for an organisation.
     */
    @Transactional
    public void markAllAsRead(UUID organisationId) {
        Page<Notification> unread = notificationRepository.findUnreadByOrganisationId(
                organisationId,
                Pageable.unpaged());

        unread.forEach(notification -> {
            notification.markAsRead();
            notificationRepository.save(notification);
        });

        log.info("Marked {} notifications as read for organisation {}", unread.getTotalElements(), organisationId);
    }

    /**
     * Delete old notifications (cleanup).
     */
    @Transactional
    public void deleteOldNotifications(UUID organisationId, int daysOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
        List<Notification> oldNotifications = notificationRepository
                .findRecentByOrganisationId(organisationId, cutoff);

        oldNotifications.forEach(notificationRepository::delete);
        log.info("Deleted {} old notifications for organisation {}", oldNotifications.size(), organisationId);
    }

    // ============= Gym-Level Notification Methods (NEW) =============

    /**
     * Get all notifications for a gym with pagination.
     */
    public Page<Notification> getGymNotifications(UUID gymId, Pageable pageable) {
        log.debug("Fetching notifications for gym: {}", gymId);
        return notificationRepository.findByGymId(gymId, pageable);
    }

    /**
     * Get unread notifications for a gym.
     */
    public Page<Notification> getUnreadGymNotifications(UUID gymId, Pageable pageable) {
        log.debug("Fetching unread notifications for gym: {}", gymId);
        return notificationRepository.findUnreadByGymId(gymId, pageable);
    }

    /**
     * Get count of unread notifications for a gym.
     */
    public long getUnreadGymCount(UUID gymId) {
        return notificationRepository.countUnreadByGymId(gymId);
    }

    /**
     * Get recent notifications for a gym (last 7 days).
     */
    public List<Notification> getRecentGymNotifications(UUID gymId, int days) {
        LocalDateTime since = LocalDateTime.now().minusDays(days);
        return notificationRepository.findRecentByGymId(gymId, since);
    }

    /**
     * Mark all notifications as read for a gym.
     */
    @Transactional
    public void markAllGymNotificationsAsRead(UUID gymId) {
        Page<Notification> unread = notificationRepository.findUnreadByGymId(
                gymId,
                Pageable.unpaged());

        unread.forEach(notification -> {
            notification.markAsRead();
            notificationRepository.save(notification);
        });

        log.info("Marked {} notifications as read for gym {}", unread.getTotalElements(), gymId);
    }

    /**
     * Delete old notifications for a gym (cleanup).
     */
    @Transactional
    public void deleteOldGymNotifications(UUID gymId, int daysOld) {
        LocalDateTime cutoff = LocalDateTime.now().minusDays(daysOld);
        List<Notification> oldNotifications = notificationRepository
                .findRecentByGymId(gymId, cutoff);

        oldNotifications.forEach(notificationRepository::delete);
        log.info("Deleted {} old notifications for gym {}", oldNotifications.size(), gymId);
    }
}
