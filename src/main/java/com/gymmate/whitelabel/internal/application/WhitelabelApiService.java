package com.gymmate.whitelabel.internal.application;

import com.gymmate.shared.domain.Strings;
import com.gymmate.whitelabel.api.WhitelabelApi;
import com.gymmate.whitelabel.api.dto.WhitelabelProfile;
import com.gymmate.whitelabel.internal.application.port.TenantMailSenders;
import com.gymmate.whitelabel.internal.application.port.WhatsAppGateway;
import com.gymmate.whitelabel.internal.domain.WhitelabelSettings;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.UUID;

/** Implementation of the whitelabel module's public facade. */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WhitelabelApiService implements WhitelabelApi {

    private final WhitelabelSettingsService settingsService;
    private final WhitelabelEncryptionService encryptionService;
    private final TenantMailSenders mailSenders;
    private final WhatsAppGateway whatsAppGateway;

    @Override
    public Optional<WhitelabelProfile> findProfile(UUID organisationId, UUID gymId) {
        return settingsService.getWhitelabelSettings(organisationId, gymId).map(WhitelabelApiService::toProfile);
    }

    @Override
    public Optional<JavaMailSender> findTenantMailSender(UUID organisationId, UUID gymId) {
        return settingsService.getWhitelabelSettings(organisationId, gymId)
                .filter(WhitelabelSettings::isSmtpEnabled)
                .map(mailSenders::getMailSender);
    }

    @Override
    public void sendWhatsApp(UUID organisationId, UUID gymId, String recipient, String text) {
        Optional<WhitelabelSettings> whitelabelOpt = settingsService.getWhitelabelSettings(organisationId, gymId);
        if (whitelabelOpt.isEmpty() || !whitelabelOpt.get().isWhatsappEnabled()) {
            throw new ChannelNotConfiguredException(
                    "WhatsApp credentials are not configured or enabled for tenant: " + organisationId);
        }
        WhitelabelSettings settings = whitelabelOpt.get();
        String apiKey = encryptionService.decrypt(settings.getWhatsappApiKeyEncrypted());
        if (!Strings.hasText(apiKey) || !Strings.hasText(settings.getWhatsappPhoneNumberId())) {
            throw new ChannelNotConfiguredException(
                    "WhatsApp Phone Number ID or API Key is missing for tenant: " + organisationId);
        }
        whatsAppGateway.send(settings.getWhatsappProvider(), settings.getWhatsappPhoneNumberId(), apiKey, recipient, text);
    }

    static WhitelabelProfile toProfile(WhitelabelSettings s) {
        return new WhitelabelProfile(s.getBrandName(), s.getLogoUrl(), s.getPrimaryColor(), s.getSecondaryColor(),
                s.getSupportEmail(), s.getSupportPhone(), s.getEmailFooterText(), s.isSmtpEnabled(),
                s.getSmtpFromEmail(), s.getSmtpFromName(), s.getSmtpUsername(), s.isWhatsappEnabled());
    }
}
