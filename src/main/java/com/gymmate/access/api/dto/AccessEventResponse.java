package com.gymmate.access.api.dto;

import com.gymmate.access.domain.AccessEvent;
import com.gymmate.access.domain.enums.AccessDecision;
import com.gymmate.access.domain.enums.AccessDirection;
import com.gymmate.access.domain.enums.DenyReason;

import java.time.LocalDateTime;
import java.util.UUID;

public record AccessEventResponse(
    UUID id,
    UUID memberId,
    UUID accessPointId,
    UUID credentialId,
    AccessDirection direction,
    AccessDecision decision,
    DenyReason denyReason,
    boolean tailgatingSuspected,
    LocalDateTime occurredAt,
    Integer validScanCount,
    Integer devicePassCount,
    String capturedImageUrl,
    String note
) {
  public static AccessEventResponse fromEntity(AccessEvent e) {
    return new AccessEventResponse(
        e.getId(), e.getMemberId(), e.getAccessPointId(), e.getCredentialId(),
        e.getDirection(), e.getDecision(), e.getDenyReason(), e.isTailgatingSuspected(),
        e.getOccurredAt(), e.getValidScanCount(), e.getDevicePassCount(),
        e.getCapturedImageUrl(), e.getNote());
  }
}
