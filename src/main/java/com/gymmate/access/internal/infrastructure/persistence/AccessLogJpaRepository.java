package com.gymmate.access.internal.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessLogJpaRepository extends JpaRepository<AccessLogJpaEntity, UUID> {

    @Query("SELECT a FROM AccessLog a WHERE a.memberId = :memberId ORDER BY a.accessTime DESC LIMIT 1")
    Optional<AccessLogJpaEntity> findTopByMemberIdOrderByAccessTimeDesc(@Param("memberId") UUID memberId);
}
