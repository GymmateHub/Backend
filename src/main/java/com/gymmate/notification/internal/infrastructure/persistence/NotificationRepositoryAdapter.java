package com.gymmate.notification.internal.infrastructure.persistence;

import com.gymmate.notification.internal.application.port.NotificationRepository;
import com.gymmate.notification.internal.domain.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.shared.constants.NotificationPriority;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link NotificationRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class NotificationRepositoryAdapter extends DomainRepositoryAdapter implements NotificationRepository {

    private final NotificationJpaRepository jpaRepository;

    public NotificationRepositoryAdapter(NotificationJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public Notification save(Notification notification) {
        return save(jpaRepository, notification);
    }

    @Override
    public Optional<Notification> findById(UUID id) {
        return this.<Optional<Notification>>fromJpa(jpaRepository.findById(id));
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
    public void delete(Notification notification) {
        delete(jpaRepository, notification);
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

    @Override
    public List<Notification> saveAll(Iterable<Notification> entities) {
        return saveAll(jpaRepository, entities);
    }

    // ============= Gym-Level Notification Methods (NEW) =============
    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<Notification> findAll() {
        return this.<List<Notification>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<Notification> findAllById(Iterable<UUID> ids) {
        return this.<List<Notification>>fromJpa(jpaRepository.findAllById(ids));
    }

    @Override
    public long count() {
        return jpaRepository.count();
    }

    @Override
    public void deleteById(UUID id) {
        jpaRepository.deleteById(id);
    }

    @Override
    public void deleteAll(Iterable<Notification> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public Notification saveAndFlush(Notification entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<Notification> findAll(Pageable pageable) {
        return this.<Page<Notification>>fromJpa(jpaRepository.findAll(pageable));
    }
}
