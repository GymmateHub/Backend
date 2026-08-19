package com.gymmate.notification.application.port;

import java.util.UUID;

/**
 * Strategy interface for resolving the AWS SES Configuration Set name for outbound emails (X-SES-CONFIGURATION-SET).
 */
public interface SesConfigurationSetResolver {

    /**
     * Resolve the SES configuration set name for the given organisation and gym context.
     *
     * @param organisationId The current tenant organisation ID
     * @param gymId          The current gym ID (if any)
     * @return Configuration set name (e.g. "gymmatehub-transactional", "gymmatehub-prod")
     */
    String resolveConfigurationSet(UUID organisationId, UUID gymId);
}
