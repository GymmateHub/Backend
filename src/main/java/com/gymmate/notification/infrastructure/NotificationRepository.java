package com.gymmate.notification.infrastructure;

import com.gymmate.notification.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for Notification.
 * Follows hexagonal architecture pattern.
 */
public interface NotificationRepository {

    Notification save(Notification notification);

    Optional<Notification> findById(UUID id);

    Page<Notification> findByOrganisationId(UUID organisationId, Pageable pageable);

    Page<Notification> findUnreadByOrganisationId(UUID organisationId, Pageable pageable);

    long countUnreadByOrganisationId(UUID organisationId);

    List<Notification> findRecentByOrganisationId(UUID organisationId, LocalDateTime since);

    List<Notification> findByOrganisationIdAndEventType(UUID organisationId, String eventType);

    void delete(Notification notification);

    // ============= Gym-Level Notification Methods (NEW) =============

    Page<Notification> findByGymId(UUID gymId, Pageable pageable);

    Page<Notification> findUnreadByGymId(UUID gymId, Pageable pageable);

    long countUnreadByGymId(UUID gymId);

    List<Notification> findRecentByGymId(UUID gymId, LocalDateTime since);

    List<Notification> findByGymIdAndEventType(UUID gymId, String eventType);
}
