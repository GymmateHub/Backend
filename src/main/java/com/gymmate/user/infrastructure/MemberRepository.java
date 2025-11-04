package com.gymmate.user.infrastructure;

import com.gymmate.user.domain.Member;
import com.gymmate.user.domain.MemberStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Spring Data JPA repository for Member entity.
 */
@Repository
public interface MemberRepository extends JpaRepository<Member, UUID> {

    // User lookup
    Optional<Member> findByUserId(UUID userId);
    boolean existsByUserId(UUID userId);

    // Membership number lookup
    Optional<Member> findByMembershipNumber(String membershipNumber);
    boolean existsByMembershipNumber(String membershipNumber);

    // Status queries
    List<Member> findByStatus(MemberStatus status);
    long countByStatus(MemberStatus status);

    // Date queries
    List<Member> findByJoinDateBetween(LocalDate startDate, LocalDate endDate);
    List<Member> findByJoinDateAfter(LocalDate date);

    // Waiver queries
    List<Member> findByWaiverSigned(boolean signed);
    List<Member> findByWaiverSignedFalse();
}

