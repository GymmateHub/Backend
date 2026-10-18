package com.gymmate.whitelabel.api;

import com.gymmate.whitelabel.api.dto.WhitelabelProfile;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.Optional;
import java.util.UUID;

/**
 * Public facade of the whitelabel module: tenant branding and the tenant's own delivery
 * channels (SMTP, WhatsApp). Credentials never leave the module.
 */
public interface WhitelabelApi {

    /** Gym-level settings when present, otherwise organisation-level settings. */
    Optional<WhitelabelProfile> findProfile(UUID organisationId, UUID gymId);

    /**
     * The tenant's SMTP sender when custom SMTP is enabled.
     *
     * @throws com.gymmate.shared.exception.DomainException SMTP_NOT_CONFIGURED when enabled but incomplete
     */
    Optional<JavaMailSender> findTenantMailSender(UUID organisationId, UUID gymId);

    /**
     * Sends a WhatsApp text through the tenant's configured provider.
     *
     * @throws ChannelNotConfiguredException when WhatsApp is not enabled or its credentials are incomplete
     * @throws com.gymmate.shared.exception.DomainException WHATSAPP_SEND_FAILED when delivery fails
     */
    void sendWhatsApp(UUID organisationId, UUID gymId, String recipient, String text);

    /** The tenant has not enabled/configured the requested channel. */
    class ChannelNotConfiguredException extends RuntimeException {
        public ChannelNotConfiguredException(String message) {
            super(message);
        }
    }
}
