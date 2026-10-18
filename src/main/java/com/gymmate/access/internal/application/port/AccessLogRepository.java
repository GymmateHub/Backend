package com.gymmate.access.internal.application.port;

import com.gymmate.shared.application.port.DomainRepository;
import com.gymmate.access.internal.domain.AccessLog;

import java.util.Optional;
import java.util.UUID;

public interface AccessLogRepository extends DomainRepository<AccessLog, UUID> {

    Optional<AccessLog> findTopByMemberIdOrderByAccessTimeDesc(UUID memberId);
}
