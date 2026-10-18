package com.gymmate.notification.internal.application.dto;

import com.gymmate.notification.internal.domain.Notification;
import com.gymmate.shared.constants.NotificationPriority;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for notification.
 */
public record NotificationResponse(
        UUID id,
        UUID organisationId,
        UUID gymId,
        String scope,
        String title,
        String message,
        NotificationPriority priority,
        String eventType,
        String metadata,
        UUID relatedEntityId,
        String relatedEntityType,
        String recipientRole,
        LocalDateTime readAt,
        boolean read,
        String deliveredVia,
        LocalDateTime deliveredAt,
        LocalDateTime createdAt
) {

    public static NotificationResponse fromEntity(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getOrganisationId(),
                notification.getGymId(),
                notification.getScope() != null ? notification.getScope().name() : null,
                notification.getTitle(),
                notification.getMessage(),
                notification.getPriority(),
                notification.getEventType(),
                notification.getMetadata(),
                notification.getRelatedEntityId(),
                notification.getRelatedEntityType(),
                notification.getRecipientRole() != null ?
                            notification.getRecipientRole().name() : null,
                notification.getReadAt(),
                notification.isRead(),
                notification.getDeliveredVia() != null ?
                            notification.getDeliveredVia().name() : null,
                notification.getDeliveredAt(),
                notification.getCreatedAt());
    }
}
