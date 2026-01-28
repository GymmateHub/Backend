package com.gymmate.notification.application.channel;

import com.gymmate.notification.domain.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * WhatsApp channel sender (stub for WhatsApp Business API integration).
 * TODO: Implement actual WhatsApp Business API integration.
 */
@Component
@Slf4j
public class WhatsAppChannelSender implements ChannelSender {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.WHATSAPP;
    }

    @Override
    public void send(String recipient, String subject, String body) throws ChannelException {
        // TODO: Implement WhatsApp Business API sending
        // For now, log and throw to trigger fallback
        log.warn("WhatsApp sending not implemented yet. Recipient: {}, Message: {}", recipient, subject);
        throw new ChannelException(NotificationChannel.WHATSAPP,
                "WhatsApp channel not yet implemented - falling back to email");
    }
}
