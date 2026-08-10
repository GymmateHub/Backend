package com.gymmate.access.api.event;

import com.gymmate.notification.events.DomainEvent;
import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.shared.multitenancy.TenantAwareEvent;
import com.gymmate.shared.multitenancy.TenantIdentity;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Published when a likely tailgating / pass-back attempt is detected.
 */
@Getter
@Builder
public class TailgatingSuspectedEvent implements DomainEvent, TenantAwareEvent {

  @Builder.Default
  private final UUID eventId = UUID.randomUUID();

  @Builder.Default
  private final LocalDateTime occurredAt = LocalDateTime.now();

  private final UUID organisationId;
  private final UUID gymId;
  private final UUID memberId;
  private final UUID accessPointId;
  private final String accessPointName;
  private final String reason;

  @Override
  public TenantIdentity getTenantIdentity() {
    return TenantIdentity.of(organisationId, gymId);
  }

  @Override
  public String getEventType() {
    return "TAILGATING_SUSPECTED";
  }

  @Override
  public String getNotificationTitle() {
    return "⚠️ Tailgating Suspected";
  }

  @Override
  public String getNotificationMessage() {
    return String.format("Possible tailgating at %s: %s",
        accessPointName != null ? accessPointName : "access point",
        reason != null ? reason : "unverified second entry");
  }

  @Override
  public NotificationPriority getPriority() {
    return NotificationPriority.HIGH;
  }
}
