package com.gymmate.health.infrastructure;

import com.gymmate.health.domain.ProgressPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for ProgressPhoto entity.
 * Provides data access operations using Spring Data JPA.
 */
@Repository
public interface ProgressPhotoJpaRepository extends JpaRepository<ProgressPhoto, UUID> {

    /**
     * Find all photos for a member ordered by date descending.
     */
    @Query("SELECT pp FROM ProgressPhoto pp WHERE pp.memberId = :memberId AND pp.active = true ORDER BY pp.photoDate DESC")
    List<ProgressPhoto> findByMemberIdOrderByDateDesc(@Param("memberId") UUID memberId);

    /**
     * Find photos for a member within date range.
     */
    @Query("SELECT pp FROM ProgressPhoto pp WHERE pp.memberId = :memberId AND pp.photoDate BETWEEN :startDate AND :endDate AND pp.active = true ORDER BY pp.photoDate DESC")
    List<ProgressPhoto> findByMemberIdAndDateRange(
        @Param("memberId") UUID memberId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find public photos for a member.
     */
    @Query("SELECT pp FROM ProgressPhoto pp WHERE pp.memberId = :memberId AND pp.isPublic = true AND pp.active = true ORDER BY pp.photoDate DESC")
    List<ProgressPhoto> findPublicPhotosByMemberId(@Param("memberId") UUID memberId);

    /**
     * Find photos by gym and date range.
     */
    @Query("SELECT pp FROM ProgressPhoto pp WHERE pp.gymId = :gymId AND pp.photoDate BETWEEN :startDate AND :endDate AND pp.active = true ORDER BY pp.photoDate DESC")
    List<ProgressPhoto> findByGymIdAndDateRange(
        @Param("gymId") UUID gymId,
        @Param("startDate") LocalDateTime startDate,
        @Param("endDate") LocalDateTime endDate
    );

    /**
     * Find latest photo for a member.
     */
    @Query("SELECT pp FROM ProgressPhoto pp WHERE pp.memberId = :memberId AND pp.active = true ORDER BY pp.photoDate DESC LIMIT 1")
    Optional<ProgressPhoto> findLatestByMemberId(@Param("memberId") UUID memberId);

    /**
     * Count photos for a member.
     */
    @Query("SELECT COUNT(pp) FROM ProgressPhoto pp WHERE pp.memberId = :memberId AND pp.active = true")
    long countByMemberId(@Param("memberId") UUID memberId);
}
