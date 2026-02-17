package com.gymmate.shared.security.invite;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Repository for UserInvite entity.
 */
@Repository
public interface UserInviteRepository extends JpaRepository<UserInvite, UUID> {

    /**
     * Find invite by token hash.
     * Primary lookup method for invite validation.
     */
    Optional<UserInvite> findByTokenHash(String tokenHash);

    /**
     * Find pending invite for email at specific gym.
     * Used to check for duplicate invites.
     */
    Optional<UserInvite> findByEmailAndGymIdAndStatus(String email, UUID gymId, InviteStatus status);

    /**
     * Find all invites for a gym (for admin dashboard).
     */
    List<UserInvite> findByGymIdOrderByCreatedAtDesc(UUID gymId);

    /**
     * Find all pending invites for a gym.
     */
    List<UserInvite> findByGymIdAndStatusOrderByCreatedAtDesc(UUID gymId, InviteStatus status);

    /**
     * Find all invites by email across all gyms.
     * Used to check if email already has pending invites elsewhere.
     */
    List<UserInvite> findByEmailAndStatus(String email, InviteStatus status);

    /**
     * Check if a pending invite exists for this email at this gym.
     */
    boolean existsByEmailAndGymIdAndStatus(String email, UUID gymId, InviteStatus status);

    /**
     * Find all expired pending invites.
     * Used by scheduled cleanup job.
     */
    @Query("SELECT i FROM UserInvite i WHERE i.status = 'PENDING' AND i.expiresAt < :now")
    List<UserInvite> findExpiredPendingInvites(@Param("now") LocalDateTime now);

    /**
     * Bulk update expired invites to EXPIRED status.
     * More efficient than loading and saving each entity.
     */
    @Modifying
    @Query("UPDATE UserInvite i SET i.status = 'EXPIRED', i.updatedAt = :now " +
           "WHERE i.status = 'PENDING' AND i.expiresAt < :now")
    int markExpiredInvites(@Param("now") LocalDateTime now);

    /**
     * Count pending invites for a gym.
     */
    long countByGymIdAndStatus(UUID gymId, InviteStatus status);

    /**
     * Delete all invites older than a certain date (for cleanup).
     */
    @Modifying
    @Query("DELETE FROM UserInvite i WHERE i.updatedAt < :cutoff AND i.status IN ('EXPIRED', 'REVOKED', 'ACCEPTED')")
    int deleteOldInvites(@Param("cutoff") LocalDateTime cutoff);
}

