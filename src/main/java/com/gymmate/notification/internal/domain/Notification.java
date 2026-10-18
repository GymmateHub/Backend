package com.gymmate.notification.internal.domain;

import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.shared.domain.TenantEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Domain entity representing a notification for admin/owner users.
 * Tracks system events and business activities that require attention.
 *
 * Supports both organisation-level and gym-level notifications:
 * - Scope=ORGANISATION: gym_id is null, visible to OWNER, ADMIN, SUPER_ADMIN
 * - Scope=GYM: gym_id is set, visible to STAFF, GYM_MANAGER (for their gym), ADMIN, OWNER, SUPER_ADMIN
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification extends TenantEntity {

    private UUID gymId;

    private String title;

    private String message;

    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    private String eventType;

    private String metadata;

    private UUID relatedEntityId;

    private String relatedEntityType;

    private RecipientRole recipientRole;

    @Builder.Default
    private NotificationScope scope = NotificationScope.ORGANISATION;

    private LocalDateTime readAt;

    private DeliveryChannel deliveredVia;

    private LocalDateTime deliveredAt;

    /**
     * Check if this notification targets a specific gym.
     */
    public boolean isGymScoped() {
        return scope == NotificationScope.GYM && gymId != null;
    }

    /**
     * Check if this notification targets the entire organisation.
     */
    public boolean isOrganisationScoped() {
        return scope == NotificationScope.ORGANISATION;
    }

    /**
     * Mark notification as read.
     */
    public void markAsRead() {
        if (this.readAt == null) {
            this.readAt = LocalDateTime.now();
        }
    }

    /**
     * Mark notification as delivered via specific channel.
     */
    public void markAsDelivered(DeliveryChannel channel) {
        this.deliveredVia = channel;
        this.deliveredAt = LocalDateTime.now();
    }

    /**
     * Check if notification has been read.
     */
    public boolean isRead() {
        return this.readAt != null;
    }

    /**
     * Check if notification has been delivered.
     */
    public boolean isDelivered() {
        return this.deliveredAt != null;
    }

    /**
     * Enum for notification scope (organisation-wide or gym-specific).
     */
    public enum NotificationScope {
        /**
         * Notification targets the entire organisation.
         */
        ORGANISATION,

        /**
         * Notification targets a specific gym within the organisation.
         */
        GYM
    }

    /**
     * Enum for recipient roles who should see this notification.
     */
    public enum RecipientRole {
        OWNER,
        ADMIN,
        STAFF,
        GYM_MANAGER,
        SUPER_ADMIN
    }

    /**
     * Enum for delivery channels used.
     */
    public enum DeliveryChannel {
        EMAIL,
        SSE,
        BOTH
    }
}
