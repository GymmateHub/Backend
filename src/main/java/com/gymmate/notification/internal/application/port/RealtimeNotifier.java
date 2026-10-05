package com.gymmate.notification.internal.application.port;

import com.gymmate.notification.internal.domain.Notification;

import java.util.UUID;

/** Outbound port: pushes notifications and e-mail delivery status to connected clients in real time. */
public interface RealtimeNotifier {

    /** @return whether at least one client connection received the notification */
    boolean sendToOrganisation(UUID organisationId, Notification notification);

    /** @return whether the user had a live connection that received the notification */
    boolean sendToUser(UUID organisationId, UUID userId, Notification notification);

    /** Publishes the delivery status (SENDING, SENT, FAILED) of a user's verification e-mail. */
    void sendEmailStatus(String userId, String status, String message);
}
