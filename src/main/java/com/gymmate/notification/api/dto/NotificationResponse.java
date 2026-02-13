package com.gymmate.notification.api.dto;

import com.gymmate.notification.domain.Notification;
import com.gymmate.notification.events.NotificationPriority;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Response DTO for notification.
 */
@Data
@Builder
public class NotificationResponse {

    private UUID id;
    private UUID organisationId;
    private UUID gymId;
    private String scope;
    private String title;
    private String message;
    private NotificationPriority priority;
    private String eventType;
    private String metadata;
    private UUID relatedEntityId;
    private String relatedEntityType;
    private String recipientRole;
    private LocalDateTime readAt;
    private boolean read;
    private String deliveredVia;
    private LocalDateTime deliveredAt;
    private LocalDateTime createdAt;

    public static NotificationResponse fromEntity(Notification notification) {
        return NotificationResponse.builder()
                .id(notification.getId())
                .organisationId(notification.getOrganisationId())
                .gymId(notification.getGymId())
                .scope(notification.getScope() != null ? notification.getScope().name() : null)
                .title(notification.getTitle())
                .message(notification.getMessage())
                .priority(notification.getPriority())
                .eventType(notification.getEventType())
                .metadata(notification.getMetadata())
                .relatedEntityId(notification.getRelatedEntityId())
                .relatedEntityType(notification.getRelatedEntityType())
                .recipientRole(notification.getRecipientRole() != null ?
                        notification.getRecipientRole().name() : null)
                .readAt(notification.getReadAt())
                .read(notification.isRead())
                .deliveredVia(notification.getDeliveredVia() != null ?
                        notification.getDeliveredVia().name() : null)
                .deliveredAt(notification.getDeliveredAt())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
