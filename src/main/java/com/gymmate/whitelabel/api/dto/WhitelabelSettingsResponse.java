package com.gymmate.whitelabel.api.dto;

import com.gymmate.whitelabel.domain.NewsletterProvider;
import com.gymmate.whitelabel.domain.SmtpSecurity;
import com.gymmate.whitelabel.domain.WhatsAppProvider;
import com.gymmate.whitelabel.domain.WhitelabelSettings;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhitelabelSettingsResponse {

    private UUID id;
    private UUID organisationId;
    private UUID gymId;

    // Branding
    private String brandName;
    private String logoUrl;
    private String faviconUrl;
    private String primaryColor;
    private String secondaryColor;
    private String customDomain;
    private String emailHeaderLogoUrl;
    private String emailFooterText;
    private String supportEmail;
    private String supportPhone;

    // SMTP Configuration
    private boolean smtpEnabled;
    private String smtpHost;
    private Integer smtpPort;
    private String smtpUsername;
    private String smtpPasswordMasked; // "••••••••" if set
    private SmtpSecurity smtpSecurity;
    private String smtpFromEmail;
    private String smtpFromName;

    // WhatsApp Configuration
    private boolean whatsappEnabled;
    private WhatsAppProvider whatsappProvider;
    private String whatsappPhoneNumber;
    private String whatsappPhoneNumberId;
    private String whatsappBusinessId;
    private String whatsappApiKeyMasked; // "••••••••" if set

    // Newsletter Configuration
    private boolean newsletterEnabled;
    private NewsletterProvider newsletterProvider;
    private String newsletterApiKeyMasked; // "••••••••" if set
    private String newsletterListId;
    private String newsletterSenderEmail;
    private String newsletterSenderName;

    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static WhitelabelSettingsResponse fromEntity(WhitelabelSettings entity, boolean isSmtpPasswordSet, boolean isWhatsAppApiKeySet, boolean isNewsletterApiKeySet) {
        if (entity == null) {
            return null;
        }

        return WhitelabelSettingsResponse.builder()
                .id(entity.getId())
                .organisationId(entity.getOrganisationId())
                .gymId(entity.getGymId())
                .brandName(entity.getBrandName())
                .logoUrl(entity.getLogoUrl())
                .faviconUrl(entity.getFaviconUrl())
                .primaryColor(entity.getPrimaryColor())
                .secondaryColor(entity.getSecondaryColor())
                .customDomain(entity.getCustomDomain())
                .emailHeaderLogoUrl(entity.getEmailHeaderLogoUrl())
                .emailFooterText(entity.getEmailFooterText())
                .supportEmail(entity.getSupportEmail())
                .supportPhone(entity.getSupportPhone())
                .smtpEnabled(entity.isSmtpEnabled())
                .smtpHost(entity.getSmtpHost())
                .smtpPort(entity.getSmtpPort())
                .smtpUsername(entity.getSmtpUsername())
                .smtpPasswordMasked(isSmtpPasswordSet ? "••••••••" : null)
                .smtpSecurity(entity.getSmtpSecurity())
                .smtpFromEmail(entity.getSmtpFromEmail())
                .smtpFromName(entity.getSmtpFromName())
                .whatsappEnabled(entity.isWhatsappEnabled())
                .whatsappProvider(entity.getWhatsappProvider())
                .whatsappPhoneNumber(entity.getWhatsappPhoneNumber())
                .whatsappPhoneNumberId(entity.getWhatsappPhoneNumberId())
                .whatsappBusinessId(entity.getWhatsappBusinessId())
                .whatsappApiKeyMasked(isWhatsAppApiKeySet ? "••••••••" : null)
                .newsletterEnabled(entity.isNewsletterEnabled())
                .newsletterProvider(entity.getNewsletterProvider())
                .newsletterApiKeyMasked(isNewsletterApiKeySet ? "••••••••" : null)
                .newsletterListId(entity.getNewsletterListId())
                .newsletterSenderEmail(entity.getNewsletterSenderEmail())
                .newsletterSenderName(entity.getNewsletterSenderName())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }
}
