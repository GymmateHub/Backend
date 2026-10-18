package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.JpaDomainRepositoryAdapter;
import com.gymmate.notification.internal.application.port.NotificationRepository;
import com.gymmate.notification.internal.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link NotificationRepository} with Spring Data JPA; CRUD comes
 * from {@link JpaDomainRepositoryAdapter}, only the Notification finders live here.
 */
@Component
@Transactional()
public class NotificationRepositoryAdapter extends JpaDomainRepositoryAdapter<Notification, UUID, NotificationJpaRepository>
        implements NotificationRepository {

    public NotificationRepositoryAdapter(NotificationJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public Page<Notification> findByOrganisationId(UUID organisationId, Pageable pageable) {
        return this.<Page<Notification>>fromJpa(jpaRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId, pageable));
    }

    @Override
    public Page<Notification> findUnreadByOrganisationId(UUID organisationId, Pageable pageable) {
        return this.<Page<Notification>>fromJpa(jpaRepository.findUnreadByOrganisationId(organisationId, pageable));
    }

    @Override
    public long countUnreadByOrganisationId(UUID organisationId) {
        return jpaRepository.countUnreadByOrganisationId(organisationId);
    }

    @Override
    public List<Notification> findRecentByOrganisationId(UUID organisationId, LocalDateTime since) {
        return this.<List<Notification>>fromJpa(jpaRepository.findRecentByOrganisationId(organisationId, since));
    }

    @Override
    public List<Notification> findByOrganisationIdAndEventType(UUID organisationId, String eventType) {
        return this.<List<Notification>>fromJpa(jpaRepository.findByOrganisationIdAndEventTypeOrderByCreatedAtDesc(organisationId, eventType));
    }

    @Override
    public Page<Notification> findByGymId(UUID gymId, Pageable pageable) {
        return this.<Page<Notification>>fromJpa(jpaRepository.findByGymIdOrderByCreatedAtDesc(gymId, pageable));
    }

    @Override
    public Page<Notification> findUnreadByGymId(UUID gymId, Pageable pageable) {
        return this.<Page<Notification>>fromJpa(jpaRepository.findUnreadByGymId(gymId, pageable));
    }

    @Override
    public long countUnreadByGymId(UUID gymId) {
        return jpaRepository.countUnreadByGymId(gymId);
    }

    @Override
    public List<Notification> findRecentByGymId(UUID gymId, LocalDateTime since) {
        return this.<List<Notification>>fromJpa(jpaRepository.findRecentByGymId(gymId, since));
    }

    @Override
    public List<Notification> findByGymIdAndEventType(UUID gymId, String eventType) {
        return this.<List<Notification>>fromJpa(jpaRepository.findByGymIdAndEventTypeOrderByCreatedAtDesc(gymId, eventType));
    }

    @Override
    public Page<Notification> findByOrganisationIdOrderByCreatedAtDesc(UUID organisationId, Pageable pageable) {
        return this.<Page<Notification>>fromJpa(jpaRepository.findByOrganisationIdOrderByCreatedAtDesc(organisationId, pageable));
    }

    @Override
    public List<Notification> findByOrganisationIdAndPriorityOrderByCreatedAtDesc(UUID organisationId, NotificationPriority priority) {
        return this.<List<Notification>>fromJpa(jpaRepository.findByOrganisationIdAndPriorityOrderByCreatedAtDesc(organisationId, priority));
    }

    @Override
    public List<Notification> findByOrganisationIdAndEventTypeOrderByCreatedAtDesc(UUID organisationId, String eventType) {
        return this.<List<Notification>>fromJpa(jpaRepository.findByOrganisationIdAndEventTypeOrderByCreatedAtDesc(organisationId, eventType));
    }

    @Override
    public List<Notification> findByOrganisationIdAndRelatedEntityIdOrderByCreatedAtDesc(UUID organisationId, UUID relatedEntityId) {
        return this.<List<Notification>>fromJpa(jpaRepository.findByOrganisationIdAndRelatedEntityIdOrderByCreatedAtDesc(organisationId, relatedEntityId));
    }

    @Override
    public Page<Notification> findByGymIdOrderByCreatedAtDesc(UUID gymId, Pageable pageable) {
        return this.<Page<Notification>>fromJpa(jpaRepository.findByGymIdOrderByCreatedAtDesc(gymId, pageable));
    }

    @Override
    public List<Notification> findByGymIdAndPriorityOrderByCreatedAtDesc(UUID gymId, NotificationPriority priority) {
        return this.<List<Notification>>fromJpa(jpaRepository.findByGymIdAndPriorityOrderByCreatedAtDesc(gymId, priority));
    }

    @Override
    public List<Notification> findByGymIdAndEventTypeOrderByCreatedAtDesc(UUID gymId, String eventType) {
        return this.<List<Notification>>fromJpa(jpaRepository.findByGymIdAndEventTypeOrderByCreatedAtDesc(gymId, eventType));
    }

    @Override
    public List<Notification> findByGymIdAndRelatedEntityIdOrderByCreatedAtDesc(UUID gymId, UUID relatedEntityId) {
        return this.<List<Notification>>fromJpa(jpaRepository.findByGymIdAndRelatedEntityIdOrderByCreatedAtDesc(gymId, relatedEntityId));
    }

    // ============= Gym-Level Notification Methods (NEW) =============
}
