package com.gymmate.membership.infrastructure;

import com.gymmate.membership.domain.MemberMembership;
import com.gymmate.membership.domain.MembershipStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * JPA repository for MemberMembership entity.
 */
@Repository
public interface MemberMembershipJpaRepository extends JpaRepository<MemberMembership, UUID> {

  List<MemberMembership> findByMemberId(UUID memberId);

  @Query("SELECT mm FROM MemberMembership mm WHERE mm.memberId = :memberId AND mm.status = 'ACTIVE' AND mm.endDate > :now")
  Optional<MemberMembership> findActiveMembershipByMemberId(@Param("memberId") UUID memberId, @Param("now") LocalDateTime now);

  List<MemberMembership> findByGymId(UUID gymId);

  List<MemberMembership> findByGymIdAndStatus(UUID gymId, MembershipStatus status);

  @Query("SELECT mm FROM MemberMembership mm WHERE mm.gymId = :gymId AND mm.status = 'ACTIVE' AND mm.endDate BETWEEN :startDate AND :endDate")
  List<MemberMembership> findExpiringMemberships(@Param("gymId") UUID gymId, @Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate);

  @Query(value = "SELECT * FROM member_memberships mm WHERE mm.plan_id = :planId", nativeQuery = true)
  List<MemberMembership> findByPlanId(@Param("planId") UUID planId);

  @Query("SELECT COUNT(mm) FROM MemberMembership mm WHERE mm.gymId = :gymId AND mm.status = 'ACTIVE'")
  long countActiveByGymId(@Param("gymId") UUID gymId);

  // Use native query to avoid Spring Data property resolution issues with generated method names
  @Query(value = "SELECT COUNT(*) FROM member_memberships mm WHERE mm.plan_id = :planId", nativeQuery = true)
  long countByPlanId(@Param("planId") UUID planId);
}
