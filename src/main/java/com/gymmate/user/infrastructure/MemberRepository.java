package com.gymmate.user.infrastructure;

import com.gymmate.user.domain.Member;
import com.gymmate.user.domain.MemberStatus;
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
public interface MemberRepository extends JpaRepository<Member, UUID> {

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

    // ========== Status queries ==========

    List<Member> findByStatus(MemberStatus status);

    long countByStatus(MemberStatus status);

    // ========== Date queries ==========

    List<Member> findByJoinDateBetween(LocalDate startDate, LocalDate endDate);

    List<Member> findByJoinDateAfter(LocalDate date);

    // ========== Waiver queries ==========

    List<Member> findByWaiverSigned(boolean signed);

    List<Member> findByWaiverSignedFalse();

    // ========== Analytics queries ==========

    @Query("SELECT COUNT(m) FROM Member m WHERE m.gymId = :gymId AND m.createdAt BETWEEN :startDate AND :endDate")
    long countByGymIdAndCreatedAtBetween(@Param("gymId") UUID gymId, @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate);
}
