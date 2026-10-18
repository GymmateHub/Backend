package com.gymmate.identity.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.identity.internal.domain.Member;
import com.gymmate.shared.constants.MemberStatus;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Member entity.
 * Provides multi-tenant aware queries.
 */
public interface MemberRepository extends DomainRepository<Member, UUID> {

    // ========== Organisation-based queries (preferred) ==========

    /**
     * Find all members in an organisation.
     */
    List<Member> findByOrganisationId(UUID organisationId);

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
    List<Member> findByOrganisationIdAndGymId(UUID organisationId, UUID gymId);

    // ========== User lookup ==========

    Optional<Member> findByUserId(UUID userId);

    boolean existsByUserId(UUID userId);

    /**
     * Find member by userId and gymId (for multi-gym membership).
     */
    Optional<Member> findByUserIdAndGymId(UUID userId, UUID gymId);

    /**
     * Check if user is a member at a specific gym.
     */
    boolean existsByUserIdAndGymId(UUID userId, UUID gymId);

    // ========== Gym queries ==========

    List<Member> findByGymId(UUID gymId);

    long countByGymId(UUID gymId);

    long countByGymIdAndStatus(UUID gymId, MemberStatus status);

    // ========== Membership number lookup ==========

    Optional<Member> findByMembershipNumber(String membershipNumber);

    boolean existsByMembershipNumber(String membershipNumber);

    // ========== Organisation + Status queries (preferred) ==========

    List<Member> findByOrganisationIdAndStatus(UUID organisationId, MemberStatus status);

    List<Member> findByOrganisationIdAndWaiverSignedFalse(UUID organisationId);

    // ========== Organisation + Date queries (preferred) ==========

    List<Member> findByOrganisationIdAndJoinDateAfter(UUID organisationId, LocalDate date);

    // ========== Status queries (legacy unscoped — prefer org-scoped variants) ==========

    List<Member> findByStatus(MemberStatus status);

    long countByStatus(MemberStatus status);

    // ========== Date queries ==========

    List<Member> findByJoinDateBetween(LocalDate startDate, LocalDate endDate);

    List<Member> findByJoinDateAfter(LocalDate date);

    // ========== Waiver queries ==========

    List<Member> findByWaiverSigned(boolean signed);

    List<Member> findByWaiverSignedFalse();

    // ========== Analytics queries ==========

    long countByGymIdAndCreatedAtBetween(UUID gymId, LocalDateTime startDate,
            LocalDateTime endDate);
}
