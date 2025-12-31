package com.gymmate.membership.infrastructure;

import com.gymmate.membership.domain.MemberPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberPaymentMethodRepository extends JpaRepository<MemberPaymentMethod, UUID> {

    @Query("SELECT p FROM MemberPaymentMethod p JOIN Member m ON p.memberId = m.userId WHERE p.memberId = :memberId AND m.gymId = :gymId ORDER BY p.isDefault DESC, p.createdAt DESC")
    List<MemberPaymentMethod> findByMemberIdAndGymIdOrderByIsDefaultDescCreatedAtDesc(@Param("memberId") UUID memberId, @Param("gymId") UUID gymId);

    @Query("SELECT p FROM MemberPaymentMethod p JOIN Member m ON p.memberId = m.userId WHERE p.memberId = :memberId AND m.gymId = :gymId AND p.isDefault = true")
    Optional<MemberPaymentMethod> findByMemberIdAndGymIdAndIsDefaultTrue(@Param("memberId") UUID memberId, @Param("gymId") UUID gymId);

    Optional<MemberPaymentMethod> findByStripePaymentMethodId(String stripePaymentMethodId);

    @Modifying
    @Query("UPDATE MemberPaymentMethod p SET p.isDefault = false WHERE p.memberId = :memberId AND p.id IN (SELECT pm.id FROM MemberPaymentMethod pm JOIN Member m ON pm.memberId = m.userId WHERE m.gymId = :gymId)")
    void clearDefaultForMember(@Param("memberId") UUID memberId, @Param("gymId") UUID gymId);

    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM MemberPaymentMethod p JOIN Member m ON p.memberId = m.userId WHERE p.memberId = :memberId AND m.gymId = :gymId")
    boolean existsByMemberIdAndGymId(@Param("memberId") UUID memberId, @Param("gymId") UUID gymId);
}

