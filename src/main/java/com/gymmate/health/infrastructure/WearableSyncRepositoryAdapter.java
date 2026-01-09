package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.WearableSource;
import com.gymmate.health.domain.WearableSync;
import com.gymmate.health.domain.WearableSyncRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing WearableSyncRepository using JPA.
 * Bridges domain layer with infrastructure layer.
 */
@Component
@RequiredArgsConstructor
public class WearableSyncRepositoryAdapter implements WearableSyncRepository {

    private final WearableSyncJpaRepository jpaRepository;

    @Override
    public WearableSync save(WearableSync wearableSync) {
        return jpaRepository.save(wearableSync);
    }

    @Override
    public Optional<WearableSync> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<WearableSync> findByMemberId(UUID memberId) {
        return jpaRepository.findByMemberIdOrderByLastSyncDesc(memberId);
    }

    @Override
    public Optional<WearableSync> findByMemberIdAndSourceType(UUID memberId, WearableSource sourceType) {
        return jpaRepository.findByMemberIdAndSourceType(memberId, sourceType);
    }

    @Override
    public List<WearableSync> findByGymId(UUID gymId) {
        return jpaRepository.findByGymIdOrderByLastSyncDesc(gymId);
    }

    @Override
    public List<WearableSync> findByStatus(String syncStatus) {
        return jpaRepository.findBySyncStatus(syncStatus);
    }

    @Override
    public List<WearableSync> findSyncsNeedingUpdate(LocalDateTime lastSyncBefore) {
        return jpaRepository.findSyncsNeedingUpdate(lastSyncBefore);
    }

    @Override
    public List<WearableSync> findFailedSyncsByGymId(UUID gymId) {
        return jpaRepository.findFailedSyncsByGymId(gymId);
    }

    @Override
    public long countByMemberId(UUID memberId) {
        return jpaRepository.countByMemberId(memberId);
    }

    @Override
    public void delete(WearableSync wearableSync) {
        wearableSync.setActive(false);
        jpaRepository.save(wearableSync);
    }

    @Override
    public boolean existsByMemberIdAndSourceType(UUID memberId, WearableSource sourceType) {
        return jpaRepository.existsByMemberIdAndSourceType(memberId, sourceType);
    }
}
