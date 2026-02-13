package com.gymmate.notification.infrastructure;

import com.gymmate.notification.domain.Notification;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository adapter implementing the domain repository interface.
 * Bridges domain layer with JPA infrastructure.
 */
@Component
@RequiredArgsConstructor
public class NotificationRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    @Override
    public Notification save(Notification notification) {
        return jpaRepository.save(notification);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public Page<Notification> findByOrganisationId(UUID organisationId, Pageable pageable) {
        return jpaRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId, pageable);
    }

    @Override
    public Page<Notification> findUnreadByOrganisationId(UUID organisationId, Pageable pageable) {
        return jpaRepository.findUnreadByOrganisationId(organisationId, pageable);
    }

    @Override
    public long countUnreadByOrganisationId(UUID organisationId) {
        return jpaRepository.countUnreadByOrganisationId(organisationId);
    }

    @Override
    public List<Notification> findRecentByOrganisationId(UUID organisationId, LocalDateTime since) {
        return jpaRepository.findRecentByOrganisationId(organisationId, since);
    }

    @Override
    public List<Notification> findByOrganisationIdAndEventType(UUID organisationId, String eventType) {
        return jpaRepository.findByOrganisationIdAndEventTypeOrderByCreatedAtDesc(organisationId, eventType);
    }

    @Override
    public void delete(Notification notification) {
        jpaRepository.delete(notification);
    }

    // ============= Gym-Level Notification Methods (NEW) =============

    @Override
    public Page<Notification> findByGymId(UUID gymId, Pageable pageable) {
        return jpaRepository.findByGymIdOrderByCreatedAtDesc(gymId, pageable);
    }

    @Override
    public Page<Notification> findUnreadByGymId(UUID gymId, Pageable pageable) {
        return jpaRepository.findUnreadByGymId(gymId, pageable);
    }

    @Override
    public long countUnreadByGymId(UUID gymId) {
        return jpaRepository.countUnreadByGymId(gymId);
    }

    @Override
    public List<Notification> findRecentByGymId(UUID gymId, LocalDateTime since) {
        return jpaRepository.findRecentByGymId(gymId, since);
    }

    @Override
    public List<Notification> findByGymIdAndEventType(UUID gymId, String eventType) {
        return jpaRepository.findByGymIdAndEventTypeOrderByCreatedAtDesc(gymId, eventType);
    }
}
