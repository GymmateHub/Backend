package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.health.internal.application.port.WearableSyncRepository;
import com.gymmate.health.internal.domain.enums.WearableSource;
import com.gymmate.health.internal.domain.WearableSync;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import com.gymmate.shared.infrastructure.persistence.DomainRepositoryAdapter;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link WearableSyncRepository} with Spring Data JPA.
 */
@Component
@Transactional()
public class WearableSyncRepositoryAdapter extends DomainRepositoryAdapter implements WearableSyncRepository {

    private final WearableSyncJpaRepository jpaRepository;

    public WearableSyncRepositoryAdapter(WearableSyncJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(contexts);
        this.jpaRepository = jpaRepository;
    }

    @Override
    public WearableSync save(WearableSync wearableSync) {
        return save(jpaRepository, wearableSync);
    }

    @Override
    public Optional<WearableSync> findById(UUID id) {
        return this.<Optional<WearableSync>>fromJpa(jpaRepository.findById(id));
    }

    @Override
    public List<WearableSync> findByMemberId(UUID memberId) {
        return this.<List<WearableSync>>fromJpa(jpaRepository.findByMemberIdOrderByLastSyncDesc(memberId));
    }

    @Override
    public Optional<WearableSync> findByMemberIdAndSourceType(UUID memberId, WearableSource sourceType) {
        return this.<Optional<WearableSync>>fromJpa(jpaRepository.findByMemberIdAndSourceType(memberId, sourceType));
    }

    @Override
    public List<WearableSync> findByGymId(UUID gymId) {
        return this.<List<WearableSync>>fromJpa(jpaRepository.findByGymIdOrderByLastSyncDesc(gymId));
    }

    @Override
    public List<WearableSync> findByStatus(String syncStatus) {
        return this.<List<WearableSync>>fromJpa(jpaRepository.findBySyncStatus(syncStatus));
    }

    @Override
    public List<WearableSync> findSyncsNeedingUpdate(LocalDateTime lastSyncBefore) {
        return this.<List<WearableSync>>fromJpa(jpaRepository.findSyncsNeedingUpdate(lastSyncBefore));
    }

    @Override
    public List<WearableSync> findFailedSyncsByGymId(UUID gymId) {
        return this.<List<WearableSync>>fromJpa(jpaRepository.findFailedSyncsByGymId(gymId));
    }

    @Override
    public long countByMemberId(UUID memberId) {
        return jpaRepository.countByMemberId(memberId);
    }

    @Override
    public void delete(WearableSync wearableSync) {
        wearableSync.setActive(false);
        save(jpaRepository, wearableSync);
    }

    @Override
    public boolean existsByMemberIdAndSourceType(UUID memberId, WearableSource sourceType) {
        return jpaRepository.existsByMemberIdAndSourceType(memberId, sourceType);
    }

    @Override
    public List<WearableSync> findByMemberIdOrderByLastSyncDesc(UUID memberId) {
        return this.<List<WearableSync>>fromJpa(jpaRepository.findByMemberIdOrderByLastSyncDesc(memberId));
    }

    @Override
    public List<WearableSync> findByGymIdOrderByLastSyncDesc(UUID gymId) {
        return this.<List<WearableSync>>fromJpa(jpaRepository.findByGymIdOrderByLastSyncDesc(gymId));
    }

    @Override
    public List<WearableSync> findBySyncStatus(String status) {
        return this.<List<WearableSync>>fromJpa(jpaRepository.findBySyncStatus(status));
    }

    @Override
    public List<WearableSync> saveAll(Iterable<WearableSync> entities) {
        return saveAll(jpaRepository, entities);
    }

    @Override
    public boolean existsById(UUID id) {
        return jpaRepository.existsById(id);
    }

    @Override
    public List<WearableSync> findAll() {
        return this.<List<WearableSync>>fromJpa(jpaRepository.findAll());
    }

    @Override
    public List<WearableSync> findAllById(Iterable<UUID> ids) {
        return this.<List<WearableSync>>fromJpa(jpaRepository.findAllById(ids));
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
    public void deleteAll(Iterable<WearableSync> entities) {
        deleteAll(jpaRepository, entities);
    }

    @Override
    public WearableSync saveAndFlush(WearableSync entity) {
        return saveAndFlush(jpaRepository, entity);
    }

    @Override
    public void flush() {
        jpaRepository.flush();
    }

    @Override
    public Page<WearableSync> findAll(Pageable pageable) {
        return this.<Page<WearableSync>>fromJpa(jpaRepository.findAll(pageable));
    }
}
