package com.gymmate.access.internal.infrastructure.persistence;

import com.gymmate.access.internal.domain.enums.AccessDecision;
import com.gymmate.access.internal.domain.enums.AccessDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessEventJpaRepository extends JpaRepository<AccessEventJpaEntity, UUID> {

    List<AccessEventJpaEntity> findByGymIdOrderByOccurredAtDesc(UUID gymId);

    List<AccessEventJpaEntity> findByGymIdAndTailgatingSuspectedTrueOrderByOccurredAtDesc(UUID gymId);

    /**
     * Most recent granted event for a member — used to derive inside/outside state.
     */
    Optional<AccessEventJpaEntity> findTopByMemberIdAndDecisionOrderByOccurredAtDesc(UUID memberId, AccessDecision decision);

    /**
     * Most recent granted entry for a credential — used for the re-entry lockout.
     */
    Optional<AccessEventJpaEntity> findTopByCredentialIdAndDecisionAndDirectionOrderByOccurredAtDesc(UUID credentialId, AccessDecision decision, AccessDirection direction);
}
