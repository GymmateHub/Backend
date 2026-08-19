package com.gymmate.notification.application;

import com.gymmate.notification.application.port.SesConfigurationSetResolver;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.UUID;

/**
 * Default implementation of SesConfigurationSetResolver.
 * Resolves the configured SES configuration set (defaults to "gymmatehub-transactional" / "gymmatehub-prod").
 */
@Component
public class DefaultSesConfigurationSetResolver implements SesConfigurationSetResolver {

    @Value("${app.email.configuration-set:gymmatehub-transactional}")
    private String defaultConfigurationSet;

    @Override
    public String resolveConfigurationSet(UUID organisationId, UUID gymId) {
        if (StringUtils.hasText(defaultConfigurationSet)) {
            return defaultConfigurationSet;
        }
        return "gymmatehub-transactional";
    }
}
