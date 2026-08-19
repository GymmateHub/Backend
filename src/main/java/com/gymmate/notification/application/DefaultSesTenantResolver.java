package com.gymmate.notification.application;

import com.gymmate.notification.application.port.SesTenantResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Default implementation of SesTenantResolver.
 * Resolves the configured SES tenant name (defaults to "gymmatehub").
 */
@Component
public class DefaultSesTenantResolver implements SesTenantResolver {

    @Value("${app.email.ses-tenant:gymmatehub}")
    private String defaultSesTenant;

    @Override
    public String resolveTenant(UUID organisationId, UUID gymId) {
        if (StringUtils.hasText(defaultSesTenant)) {
            return defaultSesTenant;
        }
        return "gymmatehub";
    }
}
