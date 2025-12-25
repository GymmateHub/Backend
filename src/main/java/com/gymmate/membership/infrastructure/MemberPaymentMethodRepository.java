package com.gymmate.membership.infrastructure;

import com.gymmate.membership.domain.MemberPaymentMethod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface MemberPaymentMethodRepository extends JpaRepository<MemberPaymentMethod, UUID> {

    List<MemberPaymentMethod> findByMemberIdAndGymIdOrderByIsDefaultDescCreatedAtDesc(UUID memberId, UUID gymId);

    Optional<MemberPaymentMethod> findByMemberIdAndGymIdAndIsDefaultTrue(UUID memberId, UUID gymId);

    Optional<MemberPaymentMethod> findByStripePaymentMethodId(String stripePaymentMethodId);

    @Modifying
    @Query("UPDATE MemberPaymentMethod p SET p.isDefault = false WHERE p.memberId = :memberId AND p.gymId = :gymId")
    void clearDefaultForMember(UUID memberId, UUID gymId);

    boolean existsByMemberIdAndGymId(UUID memberId, UUID gymId);
}

