package com.gymmate.notification.application.port;

import java.util.UUID;

/**
 * Strategy interface for resolving the AWS SES Tenant identifier for outbound emails (X-SES-TENANT).
 */
public interface SesTenantResolver {

    /**
     * Resolve the SES tenant name for the given organisation and gym context.
     *
     * @param organisationId The current tenant organisation ID
     * @param gymId          The current gym ID (if any)
     * @return SES tenant identifier string (e.g. "gymmatehub", "aegisremit", or "gym-{uuid}")
     */
    String resolveTenant(UUID organisationId, UUID gymId);
}
