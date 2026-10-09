package com.gymmate.membership.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.membership.internal.domain.MemberPaymentMethod;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MemberPaymentMethodRepository extends DomainRepository<MemberPaymentMethod, UUID> {

    List<MemberPaymentMethod> findByMemberIdAndGymIdOrderByIsDefaultDescCreatedAtDesc(UUID memberId, UUID gymId);

    Optional<MemberPaymentMethod> findByMemberIdAndGymIdAndIsDefaultTrue(UUID memberId, UUID gymId);

    Optional<MemberPaymentMethod> findByStripePaymentMethodId(String stripePaymentMethodId);

    void clearDefaultForMember(UUID memberId, UUID gymId);

    boolean existsByMemberIdAndGymId(UUID memberId, UUID gymId);
}

