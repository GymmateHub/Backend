package com.gymmate.notification.internal.application;

import com.gymmate.notification.api.NotificationApi;
import com.gymmate.shared.constants.NotificationPriority;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.UUID;

/** Implementation of the notification module's public notification facade. */
@Service
@RequiredArgsConstructor
public class NotificationApiService implements NotificationApi {

    private final NotificationService notificationService;

    @Override
    public void createAndBroadcast(String title, String message, UUID organisationId, NotificationPriority priority,
                                   String eventType, Map<String, Object> metadata) {
        notificationService.createAndBroadcast(title, message, organisationId, priority, eventType, metadata);
    }

    @Override
    public void sendToUser(UUID userId, String title, String message, NotificationPriority priority, String eventType,
                           Map<String, Object> metadata) {
        notificationService.sendToUser(userId, title, message, priority, eventType, metadata);
    }
}
