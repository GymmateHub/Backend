package com.gymmate.access.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessCredentialJpaRepository extends JpaRepository<AccessCredentialJpaEntity, UUID> {

    Optional<AccessCredentialJpaEntity> findByTokenHashAndActiveTrue(String tokenHash);

    List<AccessCredentialJpaEntity> findByMemberId(UUID memberId);

    boolean existsByTokenHash(String tokenHash);
}
