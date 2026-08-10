package com.gymmate.whitelabel.domain;

import com.gymmate.shared.domain.TenantEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.UUID;

/**
 * Entity storing whitelabeling settings for an Organisation or Gym.
 * Supports custom branding, custom SMTP settings, WhatsApp API credentials,
 * and Newsletter provider API keys.
 */
@Entity
@Table(name = "whitelabel_settings")
@Data
@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class WhitelabelSettings extends TenantEntity {

    @Column(name = "gym_id")
    private UUID gymId;

    // Branding & Customization
    @Column(name = "brand_name", length = 100)
    private String brandName;

    @Column(name = "logo_url", length = 500)
    private String logoUrl;

    @Column(name = "favicon_url", length = 500)
    private String faviconUrl;

    @Column(name = "primary_color", length = 20)
    private String primaryColor;

    @Column(name = "secondary_color", length = 20)
    private String secondaryColor;

    @Column(name = "custom_domain")
    private String customDomain;

    @Column(name = "email_header_logo_url", length = 500)
    private String emailHeaderLogoUrl;

    @Column(name = "email_footer_text", columnDefinition = "TEXT")
    private String emailFooterText;

    @Column(name = "support_email")
    private String supportEmail;

    @Column(name = "support_phone", length = 50)
    private String supportPhone;

    // Custom SMTP Configuration
    @Column(name = "smtp_enabled")
    @Builder.Default
    private boolean smtpEnabled = false;

    @Column(name = "smtp_host")
    private String smtpHost;

    @Column(name = "smtp_port")
    private Integer smtpPort;

    @Column(name = "smtp_username")
    private String smtpUsername;

    @Column(name = "smtp_password_encrypted", columnDefinition = "TEXT")
    private String smtpPasswordEncrypted;

    @Enumerated(EnumType.STRING)
    @Column(name = "smtp_security", length = 20)
    @Builder.Default
    private SmtpSecurity smtpSecurity = SmtpSecurity.STARTTLS;

    @Column(name = "smtp_from_email")
    private String smtpFromEmail;

    @Column(name = "smtp_from_name")
    private String smtpFromName;

    // WhatsApp Configuration
    @Column(name = "whatsapp_enabled")
    @Builder.Default
    private boolean whatsappEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "whatsapp_provider", length = 50)
    @Builder.Default
    private WhatsAppProvider whatsappProvider = WhatsAppProvider.META_CLOUD_API;

    @Column(name = "whatsapp_phone_number", length = 50)
    private String whatsappPhoneNumber;

    @Column(name = "whatsapp_phone_number_id", length = 100)
    private String whatsappPhoneNumberId;

    @Column(name = "whatsapp_business_id", length = 100)
    private String whatsappBusinessId;

    @Column(name = "whatsapp_api_key_encrypted", columnDefinition = "TEXT")
    private String whatsappApiKeyEncrypted;

    // Newsletter Configuration
    @Column(name = "newsletter_enabled")
    @Builder.Default
    private boolean newsletterEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "newsletter_provider", length = 50)
    @Builder.Default
    private NewsletterProvider newsletterProvider = NewsletterProvider.CUSTOM_SMTP;

    @Column(name = "newsletter_api_key_encrypted", columnDefinition = "TEXT")
    private String newsletterApiKeyEncrypted;

    @Column(name = "newsletter_list_id", length = 100)
    private String newsletterListId;

    @Column(name = "newsletter_sender_email")
    private String newsletterSenderEmail;

    @Column(name = "newsletter_sender_name")
    private String newsletterSenderName;
}
