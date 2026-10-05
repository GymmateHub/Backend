package com.gymmate.access.api.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.extern.jackson.Jacksonized;
import com.gymmate.shared.events.DomainEvent;
import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.shared.multitenancy.TenantAwareEvent;
import com.gymmate.shared.multitenancy.TenantIdentity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Published when an access attempt is denied.
 */
@Getter
@Builder
@Jacksonized
@JsonIgnoreProperties(ignoreUnknown = true)
public class AccessDeniedEvent implements DomainEvent, TenantAwareEvent {

  @Builder.Default
  private final UUID eventId = UUID.randomUUID();

  @Builder.Default
  private final LocalDateTime occurredAt = LocalDateTime.now();

  private final UUID organisationId;
  private final UUID gymId;
  private final UUID memberId;
  private final UUID accessPointId;
  private final String accessPointName;
  /** {@code DenyReason} name, e.g. NO_ACTIVE_MEMBERSHIP. */
  private final String denyReason;

  @Override
  public TenantIdentity getTenantIdentity() {
    return TenantIdentity.of(organisationId, gymId);
  }

  @Override
  public String getEventType() {
    return "ACCESS_DENIED";
  }

  @Override
  public String getNotificationTitle() {
    return "🚫 Access Denied";
  }

  @Override
  public String getNotificationMessage() {
    return String.format("Entry denied at %s (%s)",
        accessPointName != null ? accessPointName : "access point",
        denyReason);
  }

  @Override
  public NotificationPriority getPriority() {
    return NotificationPriority.MEDIUM;
  }
}
