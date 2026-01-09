package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.ProgressPhoto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Adapter implementing ProgressPhotoRepository using JPA.
 * Bridges domain layer with infrastructure layer.
 */
@Component
@RequiredArgsConstructor
public class ProgressPhotoRepositoryAdapter implements ProgressPhotoRepository {

    private final ProgressPhotoJpaRepository jpaRepository;

    @Override
    public ProgressPhoto save(ProgressPhoto progressPhoto) {
        return jpaRepository.save(progressPhoto);
    }

    @Override
    public Optional<ProgressPhoto> findById(UUID id) {
        return jpaRepository.findById(id);
    }

    @Override
    public List<ProgressPhoto> findByMemberId(UUID memberId) {
        return jpaRepository.findByMemberIdOrderByDateDesc(memberId);
    }

    @Override
    public List<ProgressPhoto> findByMemberIdAndDateRange(UUID memberId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByMemberIdAndDateRange(memberId, startDate, endDate);
    }

    @Override
    public List<ProgressPhoto> findPublicPhotosByMemberId(UUID memberId) {
        return jpaRepository.findPublicPhotosByMemberId(memberId);
    }

    @Override
    public List<ProgressPhoto> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate) {
        return jpaRepository.findByGymIdAndDateRange(gymId, startDate, endDate);
    }

    @Override
    public Optional<ProgressPhoto> findLatestByMemberId(UUID memberId) {
        return jpaRepository.findLatestByMemberId(memberId);
    }

    @Override
    public long countByMemberId(UUID memberId) {
        return jpaRepository.countByMemberId(memberId);
    }

    @Override
    public void delete(ProgressPhoto progressPhoto) {
        progressPhoto.setActive(false);
        jpaRepository.save(progressPhoto);
    }
}
