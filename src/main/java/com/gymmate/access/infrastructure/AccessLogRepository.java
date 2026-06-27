package com.gymmate.access.infrastructure;

import com.gymmate.access.domain.AccessLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AccessLogRepository extends JpaRepository<AccessLog, UUID> {

    @Query("SELECT a FROM AccessLog a WHERE a.memberId = :memberId ORDER BY a.accessTime DESC LIMIT 1")
    Optional<AccessLog> findTopByMemberIdOrderByAccessTimeDesc(@Param("memberId") UUID memberId);
}
