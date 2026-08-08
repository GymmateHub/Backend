package com.gymmate.whitelabel.api.dto;

import com.gymmate.whitelabel.domain.NewsletterProvider;
import com.gymmate.whitelabel.domain.SmtpSecurity;
import com.gymmate.whitelabel.domain.WhatsAppProvider;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WhitelabelSettingsRequest {

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
    private String smtpPassword; // Plaintext password provided by user to update
    private SmtpSecurity smtpSecurity;
    private String smtpFromEmail;
    private String smtpFromName;

    // WhatsApp Configuration
    private boolean whatsappEnabled;
    private WhatsAppProvider whatsappProvider;
    private String whatsappPhoneNumber;
    private String whatsappPhoneNumberId;
    private String whatsappBusinessId;
    private String whatsappApiKey; // Plaintext API Key/Token provided by user to update

    // Newsletter Configuration
    private boolean newsletterEnabled;
    private NewsletterProvider newsletterProvider;
    private String newsletterApiKey; // Plaintext API Key provided by user to update
    private String newsletterListId;
    private String newsletterSenderEmail;
    private String newsletterSenderName;
}
