package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.shared.infrastructure.persistence.TenantJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;
import java.time.LocalDateTime;
import java.util.UUID;
import com.gymmate.notification.internal.domain.Notification;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link Notification} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "Notification")
@Table(name = "notifications", indexes = { @Index(name = "idx_notifications_org_unread", columnList = "organisation_id, read_at"), @Index(name = "idx_notifications_org_created", columnList = "organisation_id, created_at DESC"), @Index(name = "idx_notifications_gym_unread", columnList = "gym_id, read_at"), @Index(name = "idx_notifications_gym_created", columnList = "gym_id, created_at DESC"), @Index(name = "idx_notifications_scope_org", columnList = "notification_scope, organisation_id") })
@Getter
@Setter
@NoArgsConstructor
@DomainModel(Notification.class)
public class NotificationJpaEntity extends TenantJpaEntity {

    @Column(name = "gym_id")
    private UUID gymId;

    @Column(name = "title", nullable = false, length = 200)
    private String title;

    @Column(name = "message", nullable = false, columnDefinition = "TEXT")
    private String message;

    @Enumerated(EnumType.STRING)
    @Column(name = "priority", nullable = false, length = 20)
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
    private Notification.RecipientRole recipientRole;

    @Column(name = "notification_scope", length = 20)
    @Enumerated(EnumType.STRING)
    private Notification.NotificationScope scope = Notification.NotificationScope.ORGANISATION;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "delivered_via", length = 20)
    private Notification.DeliveryChannel deliveredVia;

    @Column(name = "delivered_at")
    private LocalDateTime deliveredAt;
}
