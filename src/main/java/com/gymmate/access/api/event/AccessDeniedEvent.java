package com.gymmate.access.api.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gymmate.shared.events.DomainEvent;
import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.shared.multitenancy.TenantAwareEvent;
import com.gymmate.shared.multitenancy.TenantIdentity;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Published when an access attempt is denied.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record AccessDeniedEvent(
    UUID eventId,
    LocalDateTime occurredAt,
    UUID organisationId,
    UUID gymId,
    UUID memberId,
    UUID accessPointId,
    String accessPointName,
    /** {@code DenyReason} name, e.g. NO_ACTIVE_MEMBERSHIP. */
    String denyReason
) implements DomainEvent, TenantAwareEvent {

  public AccessDeniedEvent {
    if (eventId == null) eventId = UUID.randomUUID();
    if (occurredAt == null) occurredAt = LocalDateTime.now();
  }

  /** A new event with a fresh id, occurring now. */
  public AccessDeniedEvent(UUID organisationId, UUID gymId, UUID memberId, UUID accessPointId, String accessPointName, String denyReason) {
    this(null, null, organisationId, gymId, memberId, accessPointId, accessPointName, denyReason);
  }

  @Override
  public UUID getEventId() {
    return eventId;
  }

  @Override
  public LocalDateTime getOccurredAt() {
    return occurredAt;
  }

  @Override
  public UUID getOrganisationId() {
    return organisationId;
  }

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
