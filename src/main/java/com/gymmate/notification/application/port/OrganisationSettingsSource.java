package com.gymmate.notification.application.port;

import java.util.Optional;
import java.util.UUID;

/**
 * Raw notification-settings JSON stored on an organisation — see the port package
 * Javadoc for why {@code BroadcastService} no longer reads
 * {@code organisation.infrastructure.OrganisationRepository} directly (this was the
 * back-edge closing a gym/user/notification/organisation module cycle: notification
 * depending on organisation, which legitimately depends on gym, which legitimately
 * depends on user, which legitimately depends on notification).
 */
public interface OrganisationSettingsSource {
    Optional<String> findSettingsJson(UUID organisationId);
}
