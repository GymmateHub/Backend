package com.gymmate.access.events;

import com.gymmate.access.domain.enums.DenyReason;
import com.gymmate.notification.events.DomainEvent;
import com.gymmate.shared.constants.NotificationPriority;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Published when an access attempt is denied.
 */
@Getter
@Builder
public class AccessDeniedEvent implements DomainEvent {

  @Builder.Default
  private final UUID eventId = UUID.randomUUID();

  @Builder.Default
  private final LocalDateTime occurredAt = LocalDateTime.now();

  private final UUID organisationId;
  private final UUID gymId;
  private final UUID memberId;
  private final UUID accessPointId;
  private final String accessPointName;
  private final DenyReason denyReason;

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
