package com.gymmate.access.infrastructure;

import com.gymmate.access.domain.AccessCredential;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessCredentialRepository extends JpaRepository<AccessCredential, UUID> {

  Optional<AccessCredential> findByTokenHashAndActiveTrue(String tokenHash);

  List<AccessCredential> findByMemberId(UUID memberId);

  boolean existsByTokenHash(String tokenHash);
}
