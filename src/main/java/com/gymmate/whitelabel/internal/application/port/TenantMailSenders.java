package com.gymmate.whitelabel.internal.application.port;

import com.gymmate.whitelabel.internal.domain.WhitelabelSettings;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.UUID;

/** Outbound port: per-tenant SMTP mail senders built from whitelabel settings. */
public interface TenantMailSenders {

    /**
     * @throws com.gymmate.shared.exception.DomainException SMTP_NOT_CONFIGURED when SMTP is not enabled/configured
     */
    JavaMailSender getMailSender(WhitelabelSettings settings);

    /** Drops cached senders after the tenant's settings changed. */
    void evictCache(UUID organisationId, UUID gymId);
}
