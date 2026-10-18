package com.gymmate.access.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.access.internal.domain.AccessEvent;
import com.gymmate.access.internal.domain.enums.AccessDecision;
import com.gymmate.access.internal.domain.enums.AccessDirection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccessEventRepository extends DomainRepository<AccessEvent, UUID> {

  List<AccessEvent> findByGymIdOrderByOccurredAtDesc(UUID gymId);

  List<AccessEvent> findByGymIdAndTailgatingSuspectedTrueOrderByOccurredAtDesc(UUID gymId);

  /** Most recent granted event for a member — used to derive inside/outside state. */
  Optional<AccessEvent> findTopByMemberIdAndDecisionOrderByOccurredAtDesc(
      UUID memberId, AccessDecision decision);

  /** Most recent granted entry for a credential — used for the re-entry lockout. */
  Optional<AccessEvent> findTopByCredentialIdAndDecisionAndDirectionOrderByOccurredAtDesc(
      UUID credentialId, AccessDecision decision, AccessDirection direction);
}
