package com.gymmate.identity.internal.infrastructure.persistence;

import com.gymmate.shared.constants.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Member entity.
 * Provides multi-tenant aware queries.
 */
@Repository
public interface MemberJpaRepository extends JpaRepository<MemberJpaEntity, UUID> {

    // ========== Organisation-based queries (preferred) ==========
    /**
     * Find all members in an organisation.
     */
    List<MemberJpaEntity> findByOrganisationId(UUID organisationId);

    /**
     * Count all members in an organisation.
     */
    long countByOrganisationId(UUID organisationId);

    /**
     * Count members by organisation and status.
     */
    long countByOrganisationIdAndStatus(UUID organisationId, MemberStatus status);

    /**
     * Find members by organisation and gym.
     */
    List<MemberJpaEntity> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId);

    // ========== User lookup ==========
    Optional<MemberJpaEntity> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    /**
     * Find member by userId and gymId (for multi-gym membership).
     */
    Optional<MemberJpaEntity> findByUserIdAndGymId(UUID userId, UUID gymId);

    /**
     * Check if user is a member at a specific gym.
     */
    boolean existsByUserIdAndGymId(UUID userId, UUID gymId);

    // ========== Gym queries ==========
    List<MemberJpaEntity> findByGymId(UUID gymId);

    long countByGymId(UUID gymId);

    long countByGymIdAndStatus(UUID gymId, MemberStatus status);

    // ========== Membership number lookup ==========
    Optional<MemberJpaEntity> findByMembershipNumber(String membershipNumber);

    boolean existsByMembershipNumber(String membershipNumber);

    // ========== Organisation + Status queries (preferred) ==========
    List<MemberJpaEntity> findByOrganisationIdAndStatus(UUID organisationId, MemberStatus status);

    List<MemberJpaEntity> findByOrganisationIdAndWaiverSignedFalse(UUID organisationId);

    // ========== Organisation + Date queries (preferred) ==========
    List<MemberJpaEntity> findByOrganisationIdAndJoinDateAfter(UUID organisationId, LocalDate date);

    // ========== Status queries (legacy unscoped — prefer org-scoped variants) ==========
    List<MemberJpaEntity> findByStatus(MemberStatus status);

    long countByStatus(MemberStatus status);

    // ========== Date queries ==========
    List<MemberJpaEntity> findByJoinDateBetween(LocalDate startDate, LocalDate endDate);

    List<MemberJpaEntity> findByJoinDateAfter(LocalDate date);

    // ========== Waiver queries ==========
    List<MemberJpaEntity> findByWaiverSigned(boolean signed);

    List<MemberJpaEntity> findByWaiverSignedFalse();

    // ========== Analytics queries ==========
    @Query("SELECT COUNT(m) FROM Member m WHERE m.gymId = :gymId AND m.createdAt BETWEEN :startDate AND :endDate")
    long countByGymIdAndCreatedAtBetween(@Param("gymId") UUID gymId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
