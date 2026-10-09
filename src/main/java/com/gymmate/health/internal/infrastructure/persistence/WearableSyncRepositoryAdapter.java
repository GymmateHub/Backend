package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.SoftDeletingJpaDomainRepositoryAdapter;
import com.gymmate.health.internal.application.port.WearableSyncRepository;
import com.gymmate.health.internal.domain.enums.WearableSource;
import com.gymmate.health.internal.domain.WearableSync;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link WearableSyncRepository} with Spring Data JPA; CRUD (with soft
 * delete) comes from {@link SoftDeletingJpaDomainRepositoryAdapter}, only the WearableSync finders live here.
 */
@Component
@Transactional()
public class WearableSyncRepositoryAdapter extends SoftDeletingJpaDomainRepositoryAdapter<WearableSync, UUID, WearableSyncJpaRepository>
        implements WearableSyncRepository {

    public WearableSyncRepositoryAdapter(WearableSyncJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
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
}
