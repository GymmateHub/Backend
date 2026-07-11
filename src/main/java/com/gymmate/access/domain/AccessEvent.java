package com.gymmate.access.domain;

import com.gymmate.access.domain.enums.AccessDecision;
import com.gymmate.access.domain.enums.AccessDirection;
import com.gymmate.access.domain.enums.DenyReason;
import com.gymmate.shared.domain.GymScopedEntity;
import jakarta.persistence.*;
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
@Entity
@EqualsAndHashCode(callSuper = true)
@Builder
@Table(name = "access_events")
public class AccessEvent extends GymScopedEntity {

  @Column(name = "member_id")
  private UUID memberId;

  @Column(name = "access_point_id", nullable = false)
  private UUID accessPointId;

  @Column(name = "credential_id")
  private UUID credentialId;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  @Builder.Default
  private AccessDirection direction = AccessDirection.IN;

  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private AccessDecision decision;

  @Enumerated(EnumType.STRING)
  @Column(name = "deny_reason", length = 30)
  private DenyReason denyReason;

  @Column(name = "tailgating_suspected")
  @Builder.Default
  private boolean tailgatingSuspected = false;

  @Column(name = "occurred_at", nullable = false)
  @Builder.Default
  private LocalDateTime occurredAt = LocalDateTime.now();

  /** Valid scans counted for the entry window (hardware reconciliation). */
  @Column(name = "valid_scan_count")
  private Integer validScanCount;

  /** People detected passing through (turnstile/CV reconciliation). */
  @Column(name = "device_pass_count")
  private Integer devicePassCount;

  /** Image captured by a CV adapter for staff review. */
  @Column(name = "captured_image_url", length = 500)
  private String capturedImageUrl;

  @Column(columnDefinition = "TEXT")
  private String note;
}
