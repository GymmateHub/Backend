package com.gymmate.notification.application;

import com.gymmate.notification.domain.Notification;
import com.gymmate.notification.infrastructure.SseEmitterRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service responsible for dispatching notifications to different channels.
 * Sends notifications via SSE to connected admin users.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class NotificationDispatcher {

    private final SseEmitterRegistry sseEmitterRegistry;

    /**
     * Dispatch notification to all connected admin/owner users for the organisation.
     */
    public void dispatch(Notification notification) {
        UUID organisationId = notification.getOrganisationId();
        log.debug("Dispatching notification {} to organisation {}", notification.getId(), organisationId);

        // Send via SSE to all connected users in the organisation
        boolean sseDelivered = sseEmitterRegistry.sendToOrganisation(organisationId, notification);

        if (sseDelivered) {
            log.info("Notification {} delivered via SSE to organisation {}",
                    notification.getId(), organisationId);
        } else {
            log.debug("No SSE connections active for organisation {} - notification persisted for later retrieval",
                    organisationId);
        }
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
            log.debug("User {} not connected via SSE - notification persisted for later retrieval", userId);
        }
    }
}

