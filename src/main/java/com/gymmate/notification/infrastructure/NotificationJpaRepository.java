package com.gymmate.notification.infrastructure;

import com.gymmate.notification.domain.Notification;
import com.gymmate.notification.events.NotificationPriority;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

/**
 * JPA repository for Notification entity.
 */
@Repository
public interface NotificationJpaRepository extends JpaRepository<Notification, UUID> {

    /**
     * Find all notifications for an organisation, ordered by creation date.
     */
    Page<Notification> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId, Pageable pageable);

    /**
     * Find unread notifications for an organisation.
     */
    @Query("SELECT n FROM Notification n WHERE n.organisationId = :organisationId AND n.readAt IS NULL ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByOrganisationId(@Param("organisationId") UUID organisationId, Pageable pageable);

    /**
     * Count unread notifications for an organisation.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.organisationId = :organisationId AND n.readAt IS NULL")
    long countUnreadByOrganisationId(@Param("organisationId") UUID organisationId);

    /**
     * Find notifications by priority for an organisation.
     */
    List<Notification> findByOrganisationIdAndPriorityOrderByCreatedAtDesc(UUID organisationId, NotificationPriority priority);

    /**
     * Find recent notifications (last N days).
     */
    @Query("SELECT n FROM Notification n WHERE n.organisationId = :organisationId AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentByOrganisationId(@Param("organisationId") UUID organisationId, @Param("since") LocalDateTime since);

    /**
     * Find notifications by event type.
     */
    List<Notification> findByOrganisationIdAndEventTypeOrderByCreatedAtDesc(UUID organisationId, String eventType);

    /**
     * Find notifications by related entity.
     */
    List<Notification> findByOrganisationIdAndRelatedEntityIdOrderByCreatedAtDesc(UUID organisationId, UUID relatedEntityId);

    // ============= Gym-Level Notification Queries (NEW) =============

    /**
     * Find all notifications for a gym, ordered by creation date.
     */
    Page<Notification> findByGymIdOrderByCreatedAtDesc(UUID gymId, Pageable pageable);

    /**
     * Find unread notifications for a gym.
     */
    @Query("SELECT n FROM Notification n WHERE n.gymId = :gymId AND n.readAt IS NULL ORDER BY n.createdAt DESC")
    Page<Notification> findUnreadByGymId(@Param("gymId") UUID gymId, Pageable pageable);

    /**
     * Count unread notifications for a gym.
     */
    @Query("SELECT COUNT(n) FROM Notification n WHERE n.gymId = :gymId AND n.readAt IS NULL")
    long countUnreadByGymId(@Param("gymId") UUID gymId);

    /**
     * Find notifications by priority for a gym.
     */
    List<Notification> findByGymIdAndPriorityOrderByCreatedAtDesc(UUID gymId, NotificationPriority priority);

    /**
     * Find recent notifications for a gym (last N days).
     */
    @Query("SELECT n FROM Notification n WHERE n.gymId = :gymId AND n.createdAt >= :since ORDER BY n.createdAt DESC")
    List<Notification> findRecentByGymId(@Param("gymId") UUID gymId, @Param("since") LocalDateTime since);

    /**
     * Find notifications by event type for a gym.
     */
    List<Notification> findByGymIdAndEventTypeOrderByCreatedAtDesc(UUID gymId, String eventType);

    /**
     * Find notifications by related entity for a gym.
     */
    List<Notification> findByGymIdAndRelatedEntityIdOrderByCreatedAtDesc(UUID gymId, UUID relatedEntityId);
}
