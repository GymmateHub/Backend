package com.gymmate.access.api.dto;

import com.gymmate.access.domain.AccessEvent;
import com.gymmate.access.domain.enums.AccessDecision;
import com.gymmate.access.domain.enums.DenyReason;

import java.util.UUID;

/**
 * Outcome of a scan returned to the kiosk/turnstile/app.
 */
public record ScanResponse(
    boolean granted,
    AccessDecision decision,
    DenyReason denyReason,
    boolean tailgatingSuspected,
    UUID eventId,
    UUID memberId
) {
  public static ScanResponse fromEntity(AccessEvent event) {
    return new ScanResponse(
        event.getDecision() == AccessDecision.GRANTED,
        event.getDecision(),
        event.getDenyReason(),
        event.isTailgatingSuspected(),
        event.getId(),
        event.getMemberId()
    );
  }
}
