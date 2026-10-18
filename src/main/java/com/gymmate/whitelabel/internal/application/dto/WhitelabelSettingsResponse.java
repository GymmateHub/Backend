package com.gymmate.whitelabel.internal.application.dto;

import com.gymmate.whitelabel.internal.domain.NewsletterProvider;
import com.gymmate.whitelabel.internal.domain.SmtpSecurity;
import com.gymmate.whitelabel.internal.domain.WhatsAppProvider;
import com.gymmate.whitelabel.internal.domain.WhitelabelSettings;

import java.time.LocalDateTime;
import java.util.UUID;

public record WhitelabelSettingsResponse(
        UUID id,
        UUID organisationId,
        UUID gymId,
        // Branding
        String brandName,
        String logoUrl,
        String faviconUrl,
        String primaryColor,
        String secondaryColor,
        String customDomain,
        String emailHeaderLogoUrl,
        String emailFooterText,
        String supportEmail,
        String supportPhone,
        // SMTP Configuration
        boolean smtpEnabled,
        String smtpHost,
        Integer smtpPort,
        String smtpUsername,
        String smtpPasswordMasked, // "••••••••" if set
        SmtpSecurity smtpSecurity,
        String smtpFromEmail,
        String smtpFromName,
        // WhatsApp Configuration
        boolean whatsappEnabled,
        WhatsAppProvider whatsappProvider,
        String whatsappPhoneNumber,
        String whatsappPhoneNumberId,
        String whatsappBusinessId,
        String whatsappApiKeyMasked, // "••••••••" if set
        // Newsletter Configuration
        boolean newsletterEnabled,
        NewsletterProvider newsletterProvider,
        String newsletterApiKeyMasked, // "••••••••" if set
        String newsletterListId,
        String newsletterSenderEmail,
        String newsletterSenderName,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {

    /** Settings that have never been saved: every channel off, nothing configured. */
    public static WhitelabelSettingsResponse empty() {
        return new WhitelabelSettingsResponse(
                null, null, null, null, null, null, null, null, null, null, null, null, null,
                false, null, null, null, null, null, null, null, false, null, null, null, null,
                null, false, null, null, null, null, null, null, null);
    }

    public static WhitelabelSettingsResponse fromEntity(WhitelabelSettings entity, boolean isSmtpPasswordSet, boolean isWhatsAppApiKeySet, boolean isNewsletterApiKeySet) {
        if (entity == null) {
            return null;
        }

        return new WhitelabelSettingsResponse(
                entity.getId(),
                entity.getOrganisationId(),
                entity.getGymId(),
                entity.getBrandName(),
                entity.getLogoUrl(),
                entity.getFaviconUrl(),
                entity.getPrimaryColor(),
                entity.getSecondaryColor(),
                entity.getCustomDomain(),
                entity.getEmailHeaderLogoUrl(),
                entity.getEmailFooterText(),
                entity.getSupportEmail(),
                entity.getSupportPhone(),
                entity.isSmtpEnabled(),
                entity.getSmtpHost(),
                entity.getSmtpPort(),
                entity.getSmtpUsername(),
                isSmtpPasswordSet ? "••••••••" : null,
                entity.getSmtpSecurity(),
                entity.getSmtpFromEmail(),
                entity.getSmtpFromName(),
                entity.isWhatsappEnabled(),
                entity.getWhatsappProvider(),
                entity.getWhatsappPhoneNumber(),
                entity.getWhatsappPhoneNumberId(),
                entity.getWhatsappBusinessId(),
                isWhatsAppApiKeySet ? "••••••••" : null,
                entity.isNewsletterEnabled(),
                entity.getNewsletterProvider(),
                isNewsletterApiKeySet ? "••••••••" : null,
                entity.getNewsletterListId(),
                entity.getNewsletterSenderEmail(),
                entity.getNewsletterSenderName(),
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
