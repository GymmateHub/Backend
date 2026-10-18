package com.gymmate.whitelabel.internal.application.dto;

import com.gymmate.whitelabel.internal.domain.NewsletterProvider;
import com.gymmate.whitelabel.internal.domain.SmtpSecurity;
import com.gymmate.whitelabel.internal.domain.WhatsAppProvider;

import java.util.UUID;

public record WhitelabelSettingsRequest(
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
        String smtpPassword, // Plaintext password provided by user to update
        SmtpSecurity smtpSecurity,
        String smtpFromEmail,
        String smtpFromName,
        // WhatsApp Configuration
        boolean whatsappEnabled,
        WhatsAppProvider whatsappProvider,
        String whatsappPhoneNumber,
        String whatsappPhoneNumberId,
        String whatsappBusinessId,
        String whatsappApiKey, // Plaintext API Key/Token provided by user to update
        // Newsletter Configuration
        boolean newsletterEnabled,
        NewsletterProvider newsletterProvider,
        String newsletterApiKey, // Plaintext API Key provided by user to update
        String newsletterListId,
        String newsletterSenderEmail,
        String newsletterSenderName
) {
}
