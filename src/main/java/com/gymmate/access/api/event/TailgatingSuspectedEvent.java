package com.gymmate.access.api.event;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.gymmate.shared.events.DomainEvent;
import com.gymmate.shared.constants.NotificationPriority;
import com.gymmate.shared.multitenancy.TenantAwareEvent;
import com.gymmate.shared.multitenancy.TenantIdentity;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Published when a likely tailgating / pass-back attempt is detected.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public record TailgatingSuspectedEvent(
    UUID eventId,
    LocalDateTime occurredAt,
    UUID organisationId,
    UUID gymId,
    UUID memberId,
    UUID accessPointId,
    String accessPointName,
    String reason
) implements DomainEvent, TenantAwareEvent {

  public TailgatingSuspectedEvent {
    if (eventId == null) eventId = UUID.randomUUID();
    if (occurredAt == null) occurredAt = LocalDateTime.now();
  }

  /** A new event with a fresh id, occurring now. */
  public TailgatingSuspectedEvent(UUID organisationId, UUID gymId, UUID memberId, UUID accessPointId, String accessPointName, String reason) {
    this(null, null, organisationId, gymId, memberId, accessPointId, accessPointName, reason);
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
