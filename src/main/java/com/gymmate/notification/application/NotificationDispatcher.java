package com.gymmate.notification.application;

import com.gymmate.notification.domain.Notification;
import com.gymmate.notification.infrastructure.NotificationRepository;
import com.gymmate.notification.infrastructure.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

/**
 * Service responsible for dispatching notifications to different channels.
 * Sends notifications via SSE to connected admin users, and optionally via
 * AWS SNS when {@code aws.sns.enabled=true}.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final SseEmitterRegistry sseEmitterRegistry;
    private final NotificationRepository notificationRepository;
    private final Optional<SnsNotificationPublisher> snsPublisher;

    /**
     * Dispatch notification to all connected admin/owner users for the organisation.
     */
    public void dispatch(Notification notification) {
        UUID organisationId = notification.getOrganisationId();
        log.debug("Dispatching notification {} to organisation {}", notification.getId(), organisationId);

        boolean sseDelivered = sseEmitterRegistry.sendToOrganisation(organisationId, notification);

        if (sseDelivered) {
            log.info("Notification {} delivered via SSE to organisation {}",
                    notification.getId(), organisationId);
        } else {
            log.debug("No SSE connections active for organisation {} - notification persisted for later retrieval",
                    organisationId);
        }

        boolean snsDelivered = publishToSns(notification);
        persistDeliveryState(notification, sseDelivered, snsDelivered);
    }

    /**
     * Dispatch notification to a specific user.
     */
    public void dispatchToUser(UUID organisationId, UUID userId, Notification notification) {
        log.debug("Dispatching notification {} to user {} in organisation {}",
                notification.getId(), userId, organisationId);

        boolean delivered = sseEmitterRegistry.sendToUser(organisationId, userId, notification);

        if (delivered) {
            log.info("Notification {} delivered via SSE to user {}", notification.getId(), userId);
        } else {
            log.debug("User {} not connected via SSE - delivering via SNS if enabled", userId);
        }

        boolean snsDelivered = publishToSns(notification);
        if (delivered && snsDelivered) {
            notification.markAsDelivered(Notification.DeliveryChannel.BOTH);
        } else if (delivered) {
            notification.markAsDelivered(Notification.DeliveryChannel.SSE);
        } else if (snsDelivered) {
            notification.markAsDelivered(Notification.DeliveryChannel.SNS);
        }
    }

    private boolean publishToSns(Notification notification) {
        return snsPublisher.map(publisher -> publisher.publish(notification)).orElse(false);
    }

    private void persistDeliveryState(Notification notification, boolean sseDelivered, boolean snsDelivered) {
        if (sseDelivered && snsDelivered) {
            notification.markAsDelivered(Notification.DeliveryChannel.BOTH);
        } else if (sseDelivered) {
            notification.markAsDelivered(Notification.DeliveryChannel.SSE);
        } else if (snsDelivered) {
            notification.markAsDelivered(Notification.DeliveryChannel.SNS);
        }

        notificationRepository.save(notification);
    }
}
