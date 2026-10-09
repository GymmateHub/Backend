package com.gymmate.health.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.SoftDeletingJpaDomainRepositoryAdapter;
import com.gymmate.health.internal.application.port.ProgressPhotoRepository;
import com.gymmate.health.internal.domain.ProgressPhoto;
import org.springframework.stereotype.Component;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import com.gymmate.shared.infrastructure.persistence.DomainPersistenceContexts;
import org.springframework.transaction.annotation.Transactional;

/**
 * Persistence adapter implementing {@link ProgressPhotoRepository} with Spring Data JPA; CRUD (with soft
 * delete) comes from {@link SoftDeletingJpaDomainRepositoryAdapter}, only the ProgressPhoto finders live here.
 */
@Component
@Transactional()
public class ProgressPhotoRepositoryAdapter extends SoftDeletingJpaDomainRepositoryAdapter<ProgressPhoto, UUID, ProgressPhotoJpaRepository>
        implements ProgressPhotoRepository {

    public ProgressPhotoRepositoryAdapter(ProgressPhotoJpaRepository jpaRepository, DomainPersistenceContexts contexts) {
        super(jpaRepository, contexts);
    }

    @Override
    public List<ProgressPhoto> findByMemberId(UUID memberId) {
        return this.<List<ProgressPhoto>>fromJpa(jpaRepository.findByMemberIdOrderByDateDesc(memberId));
    }

    @Override
    public List<ProgressPhoto> findByMemberIdAndDateRange(UUID memberId, LocalDateTime startDate, LocalDateTime endDate) {
        return this.<List<ProgressPhoto>>fromJpa(jpaRepository.findByMemberIdAndDateRange(memberId, startDate, endDate));
    }

    @Override
    public List<ProgressPhoto> findPublicPhotosByMemberId(UUID memberId) {
        return this.<List<ProgressPhoto>>fromJpa(jpaRepository.findPublicPhotosByMemberId(memberId));
    }

    @Override
    public List<ProgressPhoto> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return this.<List<ProgressPhoto>>fromJpa(jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate));
    }

    @Override
    public Optional<ProgressPhoto> findLatestByMemberId(UUID memberId) {
        return this.<Optional<ProgressPhoto>>fromJpa(jpaRepository.findLatestByMemberId(memberId));
    }

    @Override
    public long countByMemberId(UUID memberId) {
        return jpaRepository.countByMemberId(memberId);
    }

    @Override
    public List<ProgressPhoto> findByMemberIdOrderByDateDesc(UUID memberId) {
        return this.<List<ProgressPhoto>>fromJpa(jpaRepository.findByMemberIdOrderByDateDesc(memberId));
    }
}
