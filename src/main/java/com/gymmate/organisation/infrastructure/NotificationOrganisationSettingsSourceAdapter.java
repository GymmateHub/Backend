package com.gymmate.organisation.infrastructure;

import com.gymmate.notification.api.spi.OrganisationSettingsSource;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.UUID;

/**
 * Implements notification's {@link OrganisationSettingsSource} port using this
 * module's own {@code OrganisationRepository} — see the port Javadoc for why
 * {@code BroadcastService} no longer calls it directly.
 */
@Component
@RequiredArgsConstructor
public class NotificationOrganisationSettingsSourceAdapter implements OrganisationSettingsSource {

    private final OrganisationRepository organisationRepository;

    @Override
    public Optional<String> findSettingsJson(UUID organisationId) {
        return organisationRepository.findById(organisationId)
                .map(org -> org.getSettings());
    }
}
