package com.gymmate.access.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.access.internal.domain.AccessCredential;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface AccessCredentialRepository extends DomainRepository<AccessCredential, UUID> {

  Optional<AccessCredential> findByTokenHashAndActiveTrue(String tokenHash);

  List<AccessCredential> findByMemberId(UUID memberId);

  boolean existsByTokenHash(String tokenHash);
}
