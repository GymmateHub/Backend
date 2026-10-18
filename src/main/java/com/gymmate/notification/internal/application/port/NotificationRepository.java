package com.gymmate.notification.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.notification.internal.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.gymmate.shared.constants.NotificationPriority;

/**
 * Domain repository interface for Notification.
 * Follows hexagonal architecture pattern.
 */
public interface NotificationRepository extends DomainRepository<Notification, UUID> {

    Page<Notification> findByOrganisationId(UUID organisationId, Pageable pageable);

    Page<Notification> findUnreadByOrganisationId(UUID organisationId, Pageable pageable);

    long countUnreadByOrganisationId(UUID organisationId);

    List<Notification> findRecentByOrganisationId(UUID organisationId, LocalDateTime since);

    List<Notification> findByOrganisationIdAndEventType(UUID organisationId, String eventType);

    // ============= Gym-Level Notification Methods (NEW) =============

    Page<Notification> findByGymId(UUID gymId, Pageable pageable);

    Page<Notification> findUnreadByGymId(UUID gymId, Pageable pageable);

    long countUnreadByGymId(UUID gymId);

    List<Notification> findRecentByGymId(UUID gymId, LocalDateTime since);

    List<Notification> findByGymIdAndEventType(UUID gymId, String eventType);

    Page<Notification> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId, Pageable pageable);

    List<Notification> findByOrganisationIdAndPriorityOrderByCreatedAtDesc(UUID organisationId, NotificationPriority priority);

    List<Notification> findByOrganisationIdAndEventTypeOrderByCreatedAtDesc(UUID organisationId, String eventType);

    List<Notification> findByOrganisationIdAndRelatedEntityIdOrderByCreatedAtDesc(UUID organisationId, UUID relatedEntityId);

    Page<Notification> findByGymIdOrderByCreatedAtDesc(UUID gymId, Pageable pageable);

    List<Notification> findByGymIdAndPriorityOrderByCreatedAtDesc(UUID gymId, NotificationPriority priority);

    List<Notification> findByGymIdAndEventTypeOrderByCreatedAtDesc(UUID gymId, String eventType);

    List<Notification> findByGymIdAndRelatedEntityIdOrderByCreatedAtDesc(UUID gymId, UUID relatedEntityId);
}
