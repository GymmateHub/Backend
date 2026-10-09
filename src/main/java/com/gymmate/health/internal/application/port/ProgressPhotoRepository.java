package com.gymmate.health.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.health.internal.domain.ProgressPhoto;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Domain repository interface for ProgressPhoto.
 * Defines domain-level operations for managing progress photos.
 */
public interface ProgressPhotoRepository extends DomainRepository<ProgressPhoto, UUID> {

    /**
     * Find all photos for a member.
     */
    List<ProgressPhoto> findByMemberId(UUID memberId);

    /**
     * Find photos for a member within date range.
     */
    List<ProgressPhoto> findByMemberIdAndDateRange(UUID memberId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find public photos for a member (visible to trainers/gym).
     */
    List<ProgressPhoto> findPublicPhotosByMemberId(UUID memberId);

    /**
     * Find photos by gym and date range.
     */
    List<ProgressPhoto> findByGymIdAndDateRange(UUID gymId, LocalDateTime startDate, LocalDateTime endDate);

    /**
     * Find latest photo for a member.
     */
    Optional<ProgressPhoto> findLatestByMemberId(UUID memberId);

    /**
     * Count photos for a member.
     */
    long countByMemberId(UUID memberId);

    /**
     * Delete a progress photo (soft delete).
     */
    void delete(ProgressPhoto progressPhoto);

    List<ProgressPhoto> findByMemberIdOrderByDateDesc(UUID memberId);
}
