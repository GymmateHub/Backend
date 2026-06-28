package com.gymmate.access.infrastructure;

import com.gymmate.access.domain.AccessEvent;
import com.gymmate.access.domain.enums.AccessDecision;
import com.gymmate.access.domain.enums.AccessDirection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessEventRepository extends JpaRepository<AccessEvent, UUID> {

  List<AccessEvent> findByGymIdOrderByOccurredAtDesc(UUID gymId);

  List<AccessEvent> findByGymIdAndTailgatingSuspectedTrueOrderByOccurredAtDesc(UUID gymId);

  /** Most recent granted event for a member — used to derive inside/outside state. */
  Optional<AccessEvent> findTopByMemberIdAndDecisionOrderByOccurredAtDesc(
      UUID memberId, AccessDecision decision);

  /** Most recent granted entry for a credential — used for the re-entry lockout. */
  Optional<AccessEvent> findTopByCredentialIdAndDecisionAndDirectionOrderByOccurredAtDesc(
      UUID credentialId, AccessDecision decision, AccessDirection direction);
}
