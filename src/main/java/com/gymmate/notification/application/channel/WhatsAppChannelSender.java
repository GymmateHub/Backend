package com.gymmate.notification.application.channel;

import com.gymmate.notification.domain.NotificationChannel;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.whitelabel.application.WhitelabelEncryptionService;
import com.gymmate.whitelabel.application.WhitelabelSettingsService;
import com.gymmate.whitelabel.domain.WhitelabelSettings;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Optional;
import java.util.UUID;

/**
 * WhatsApp channel sender utilizing tenant-configured WhatsApp Business API credentials.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class WhatsAppChannelSender implements ChannelSender {

    private final WhitelabelSettingsService whitelabelSettingsService;
    private final WhitelabelEncryptionService encryptionService;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.WHATSAPP;
    }

    @Override
    public void send(String recipient, String subject, String body) throws ChannelException {
        UUID organisationId = TenantContext.getCurrentTenantId();
        UUID gymId = TenantContext.getCurrentGymId();

        Optional<WhitelabelSettings> whitelabelOpt = whitelabelSettingsService.getWhitelabelSettings(organisationId, gymId);

        if (whitelabelOpt.isEmpty() || !whitelabelOpt.get().isWhatsappEnabled()) {
            throw new ChannelException(NotificationChannel.WHATSAPP,
                    "WhatsApp credentials are not configured or enabled for tenant: " + organisationId);
        }

        WhitelabelSettings settings = whitelabelOpt.get();
        String apiKey = encryptionService.decrypt(settings.getWhatsappApiKeyEncrypted());

        if (!StringUtils.hasText(apiKey) || !StringUtils.hasText(settings.getWhatsappPhoneNumberId())) {
            throw new ChannelException(NotificationChannel.WHATSAPP,
                    "WhatsApp Phone Number ID or API Key is missing for tenant: " + organisationId);
        }

        try {
            whitelabelSettingsService.sendWhatsAppMessage(
                    settings.getWhatsappProvider(),
                    settings.getWhatsappPhoneNumberId(),
                    apiKey,
                    recipient,
                    subject != null ? subject + "\n" + body : body
            );
        } catch (Exception e) {
            throw new ChannelException(NotificationChannel.WHATSAPP, e.getMessage());
        }
    }
}
