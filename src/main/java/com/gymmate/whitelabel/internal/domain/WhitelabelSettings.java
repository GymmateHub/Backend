package com.gymmate.whitelabel.internal.domain;

import com.gymmate.shared.domain.TenantEntity;
import lombok.*;

import java.util.UUID;

/**
 * Entity storing whitelabeling settings for an Organisation or Gym.
 * Supports custom branding, custom SMTP settings, WhatsApp API credentials,
 * and Newsletter provider API keys.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WhitelabelSettings extends TenantEntity {

    private UUID gymId;

    // Branding & Customization
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

    // Custom SMTP Configuration
    @Builder.Default
    private 
    boolean smtpEnabled = false;

    private String smtpHost;

    private Integer smtpPort;

    private String smtpUsername;

    private String smtpPasswordEncrypted;

    @Builder.Default
    private SmtpSecurity smtpSecurity = SmtpSecurity.STARTTLS;

    private String smtpFromEmail;

    private String smtpFromName;

    // WhatsApp Configuration
    @Builder.Default
    private 
    boolean whatsappEnabled = false;

    @Builder.Default
    private WhatsAppProvider whatsappProvider = WhatsAppProvider.META_CLOUD_API;

    private String whatsappPhoneNumber;

    private String whatsappPhoneNumberId;

    private String whatsappBusinessId;

    private String whatsappApiKeyEncrypted;

    // Newsletter Configuration
    @Builder.Default
    private 
    boolean newsletterEnabled = false;

    @Builder.Default
    private NewsletterProvider newsletterProvider = NewsletterProvider.CUSTOM_SMTP;

    private String newsletterApiKeyEncrypted;

    private String newsletterListId;

    private String newsletterSenderEmail;

    private String newsletterSenderName;
}
