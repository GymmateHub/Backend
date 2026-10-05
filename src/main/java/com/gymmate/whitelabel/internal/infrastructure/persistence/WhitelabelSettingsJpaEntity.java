package com.gymmate.whitelabel.internal.infrastructure.persistence;

import com.gymmate.shared.infrastructure.persistence.TenantJpaEntity;
import jakarta.persistence.*;
import lombok.*;
import java.util.UUID;
import com.gymmate.whitelabel.internal.domain.NewsletterProvider;
import com.gymmate.whitelabel.internal.domain.WhitelabelSettings;
import com.gymmate.whitelabel.internal.domain.SmtpSecurity;
import com.gymmate.whitelabel.internal.domain.WhatsAppProvider;
import com.gymmate.shared.infrastructure.persistence.DomainModel;

/**
 * Persistence model of {@link WhitelabelSettings} (state and mapping only; behaviour lives in the domain class).
 */
@Entity(name = "WhitelabelSettings")
@Table(name = "whitelabel_settings")
@Getter
@Setter
@NoArgsConstructor
@DomainModel(WhitelabelSettings.class)
public class WhitelabelSettingsJpaEntity extends TenantJpaEntity {

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
    private SmtpSecurity smtpSecurity = SmtpSecurity.STARTTLS;

    @Column(name = "smtp_from_email")
    private String smtpFromEmail;

    @Column(name = "smtp_from_name")
    private String smtpFromName;

    // WhatsApp Configuration
    @Column(name = "whatsapp_enabled")
    private boolean whatsappEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "whatsapp_provider", length = 50)
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
    private boolean newsletterEnabled = false;

    @Enumerated(EnumType.STRING)
    @Column(name = "newsletter_provider", length = 50)
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
