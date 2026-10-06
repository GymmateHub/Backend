package com.gymmate.membership.internal.infrastructure.persistence;

import com.gymmate.membership.internal.domain.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.NativeQuery;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for MemberMembership entity.
 */
@Repository
public interface MemberMembershipJpaRepository extends JpaRepository<MemberMembershipJpaEntity, UUID> {

    List<MemberMembershipJpaEntity> findByMemberId(UUID memberId);

    @Query("SELECT mm FROM MemberMembership mm WHERE mm.memberId = :memberId AND mm.status = 'ACTIVE' AND mm.endDate > :now")
    Optional<MemberMembershipJpaEntity> findActiveMembershipByMemberId(@Param("memberId") UUID memberId, @Param("now") LocalDateTime now);

    @Query("SELECT mm FROM MemberMembership mm WHERE mm.gymId = :gymId")
    List<MemberMembershipJpaEntity> findByGymId(@Param("gymId") UUID gymId);

    @Query("SELECT mm FROM MemberMembership mm WHERE mm.gymId = :gymId AND mm.status = :status")
    List<MemberMembershipJpaEntity> findByGymIdAndStatus(@Param("gymId") UUID gymId, @Param("status") MembershipStatus status);

    @Query("SELECT mm FROM MemberMembership mm WHERE mm.memberId = :memberId AND mm.gymId = :gymId AND mm.status IN :statuses")
    List<MemberMembershipJpaEntity> findByMemberIdAndGymIdAndStatusIn(@Param("memberId") UUID memberId, @Param("gymId") UUID gymId, @Param("statuses") List<MembershipStatus> statuses);

    Optional<MemberMembershipJpaEntity> findByStripeSubscriptionId(String stripeSubscriptionId);

    @Query("SELECT mm FROM MemberMembership mm WHERE mm.gymId = :gymId AND mm.status = 'ACTIVE' AND mm.endDate BETWEEN :startDate AND :endDate")
    List<MemberMembershipJpaEntity> findExpiringMemberships(@Param("gymId") UUID gymId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @NativeQuery("SELECT * FROM member_memberships mm WHERE mm.membership_plan_id = :planId")
    List<MemberMembershipJpaEntity> findByPlanId(@Param("planId") UUID planId);

    @Query("SELECT COUNT(mm) FROM MemberMembership mm WHERE mm.gymId = :gymId AND mm.status = 'ACTIVE'")
    long countActiveByGymId(@Param("gymId") UUID gymId);

    // Use native query to avoid Spring Data property resolution issues with
    // generated method names
    @NativeQuery("SELECT COUNT(*) FROM member_memberships mm WHERE mm.membership_plan_id = :planId")
    long countByPlanId(@Param("planId") UUID planId);

    @Query("SELECT mm FROM MemberMembership mm WHERE mm.frozen = true AND mm.frozenUntil < :date")
    List<MemberMembershipJpaEntity> findFrozenMembershipsToUnfreeze(@Param("date") java.time.LocalDate date);

    // ===== Expiry Enforcement Queries =====
    /**
     * Find active memberships that have passed their end date and are NOT set to auto-renew.
     * These should be marked as EXPIRED by the scheduled task.
     */
    @Query("SELECT mm FROM MemberMembership mm WHERE mm.status = 'ACTIVE' AND mm.endDate < :today AND mm.autoRenew = false")
    List<MemberMembershipJpaEntity> findExpiredActiveMemberships(@Param("today") LocalDateTime today);

    /**
     * Find active memberships that have passed their end date and ARE set to auto-renew.
     * These should be renewed by the scheduled task.
     */
    @Query("SELECT mm FROM MemberMembership mm WHERE mm.status = 'ACTIVE' AND mm.endDate < :today AND mm.autoRenew = true")
    List<MemberMembershipJpaEntity> findAutoRenewExpiredMemberships(@Param("today") LocalDateTime today);

    /**
     * Memberships past due for longer than the grace period — candidates for
     * escalation to SUSPENDED. See MembershipService.escalatePastDueMemberships.
     */
    @Query("SELECT mm FROM MemberMembership mm WHERE mm.status = 'PAST_DUE' AND mm.pastDueSince < :cutoff")
    List<MemberMembershipJpaEntity> findStalePastDueMemberships(@Param("cutoff") LocalDateTime cutoff);

    // ===== Analytics Queries =====
    @Query("SELECT COUNT(mm) FROM MemberMembership mm WHERE mm.gymId = :gymId AND mm.status = :status")
    long countByGymIdAndStatus(@Param("gymId") UUID gymId, @Param("status") MembershipStatus status);

    @Query("SELECT COUNT(mm) FROM MemberMembership mm WHERE mm.gymId = :gymId AND mm.status = 'CANCELLED' AND mm.createdAt BETWEEN :startDate AND :endDate")
    long countCancelledByGymIdAndDateRange(@Param("gymId") UUID gymId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

    @Query("SELECT mp.name, COUNT(mm) FROM MemberMembership mm JOIN MembershipPlan mp ON mm.membershipPlanId = mp.id WHERE mm.gymId = :gymId AND mm.status = 'ACTIVE' GROUP BY mp.name")
    List<Object[]> countActiveMembersByPlan(@Param("gymId") UUID gymId);

    @Query("SELECT SUM(mm.monthlyAmount) FROM MemberMembership mm WHERE mm.gymId = :gymId AND mm.status = 'ACTIVE' AND mm.nextBillingDate BETWEEN :startDate AND :endDate")
    BigDecimal sumProjectedRevenueByGymIdAndDateRange(@Param("gymId") UUID gymId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);
}
