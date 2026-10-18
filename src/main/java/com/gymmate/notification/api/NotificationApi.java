package com.gymmate.notification.api;

import com.gymmate.shared.constants.NotificationPriority;

import java.util.Map;
import java.util.UUID;

/** Public facade for in-app notifications. */
public interface NotificationApi {

    /** Stores an organisation-wide notification and pushes it to connected clients. */
    void createAndBroadcast(String title, String message, UUID organisationId, NotificationPriority priority,
                            String eventType, Map<String, Object> metadata);

    /** Notifies a single user. */
    void sendToUser(UUID userId, String title, String message, NotificationPriority priority, String eventType,
                    Map<String, Object> metadata);
}
