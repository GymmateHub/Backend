package com.gymmate.notification.internal.infrastructure.integration;

import com.gymmate.notification.internal.application.port.ChannelException;
import com.gymmate.notification.internal.application.port.ChannelSender;
import com.gymmate.notification.internal.domain.NotificationChannel;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.whitelabel.api.WhitelabelApi;
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

    private final WhitelabelApi whitelabelApi;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.WHATSAPP;
    }

    @Override
    public void send(String recipient, String subject, String body) throws ChannelException {
        UUID organisationId = TenantContext.getCurrentTenantId();
        UUID gymId = TenantContext.getCurrentGymId();
        try {
            whitelabelApi.sendWhatsApp(organisationId, gymId, recipient,
                    subject != null ? subject + "\n" + body : body);
        } catch (Exception e) {
            throw new ChannelException(NotificationChannel.WHATSAPP, e.getMessage());
        }
    }
}
