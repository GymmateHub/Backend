package com.gymmate.notification.domain;

import com.gymmate.notification.events.NotificationPriority;
import com.gymmate.shared.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

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
@Entity
@Table(name = "notifications", indexes = {
        @Index(name = "idx_notifications_org_unread", columnList = "organisation_id, read_at"),
        @Index(name = "idx_notifications_org_created", columnList = "organisation_id, created_at DESC"),
        @Index(name = "idx_notifications_gym_unread", columnList = "gym_id, read_at"),
        @Index(name = "idx_notifications_gym_created", columnList = "gym_id, created_at DESC"),
        @Index(name = "idx_notifications_scope_org", columnList = "notification_scope, organisation_id")
})
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification extends TenantEntity {

    @Column(name = "gym_id")
    private UUID gymId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
    @Builder.Default
    private NotificationPriority priority = NotificationPriority.MEDIUM;

    @Column(name = "event_type", nullable = false, length = 50)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "metadata", columnDefinition = "jsonb")
    private String metadata;

    @Column(name = "related_entity_id")
    private UUID relatedEntityId;

    @Column(name = "related_entity_type", length = 50)
    private String relatedEntityType;

    @Enumerated(EnumType.STRING)
    @Column(name = "recipient_role", length = 20)
    private RecipientRole recipientRole;

    @Column(name = "notification_scope", length = 20)
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private NotificationScope scope = NotificationScope.ORGANISATION;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivered_via", length = 20)
    private DeliveryChannel deliveredVia;

    @Column(name = "delivered_at")
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
