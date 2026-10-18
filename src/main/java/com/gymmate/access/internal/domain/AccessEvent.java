package com.gymmate.access.internal.domain;

import com.gymmate.access.internal.domain.enums.AccessDecision;
import com.gymmate.access.internal.domain.enums.AccessDirection;
import com.gymmate.access.internal.domain.enums.DenyReason;
import com.gymmate.shared.domain.GymScopedEntity;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Append-only record of an access attempt — the audit trail / Visitors log and
 * the source for tailgating reports.
 */
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
@Builder
public class AccessEvent extends GymScopedEntity {

  private UUID memberId;

  private UUID accessPointId;

  private UUID credentialId;

  @Builder.Default
  private AccessDirection direction = AccessDirection.IN;

  private AccessDecision decision;

  private DenyReason denyReason;

  @Builder.Default
  private boolean tailgatingSuspected = false;

  @Builder.Default
  private LocalDateTime occurredAt = LocalDateTime.now();

  /** Valid scans counted for the entry window (hardware reconciliation). */
  private Integer validScanCount;

  /** People detected passing through (turnstile/CV reconciliation). */
  private Integer devicePassCount;

  /** Image captured by a CV adapter for staff review. */
  private String capturedImageUrl;

  private String note;
}
