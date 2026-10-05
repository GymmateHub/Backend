package com.gymmate.access.internal.application.port;

import com.gymmate.access.internal.domain.AccessEvent;
import com.gymmate.access.internal.domain.enums.AccessDecision;
import com.gymmate.access.internal.domain.enums.AccessDirection;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public interface AccessEventRepository {

  List<AccessEvent> findByGymIdOrderByOccurredAtDesc(UUID gymId);

  List<AccessEvent> findByGymIdAndTailgatingSuspectedTrueOrderByOccurredAtDesc(UUID gymId);

  /** Most recent granted event for a member — used to derive inside/outside state. */
  Optional<AccessEvent> findTopByMemberIdAndDecisionOrderByOccurredAtDesc(
      UUID memberId, AccessDecision decision);

  /** Most recent granted entry for a credential — used for the re-entry lockout. */
  Optional<AccessEvent> findTopByCredentialIdAndDecisionAndDirectionOrderByOccurredAtDesc(
      UUID credentialId, AccessDecision decision, AccessDirection direction);
  
  AccessEvent save(AccessEvent entity);
  
  List<AccessEvent> saveAll(Iterable<AccessEvent> entities);
  
  Optional<AccessEvent> findById(UUID id);
  
  boolean existsById(UUID id);
  
  List<AccessEvent> findAll();
  
  List<AccessEvent> findAllById(Iterable<UUID> ids);
  
  long count();
  
  void deleteById(UUID id);
  
  void delete(AccessEvent entity);
  
  void deleteAll(Iterable<AccessEvent> entities);
  
  AccessEvent saveAndFlush(AccessEvent entity);
  
  void flush();
  
  Page<AccessEvent> findAll(Pageable pageable);
}
