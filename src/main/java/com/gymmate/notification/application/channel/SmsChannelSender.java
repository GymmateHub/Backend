package com.gymmate.notification.application.channel;

import com.gymmate.notification.domain.NotificationChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * SMS channel sender (stub for Twilio integration).
 * TODO: Implement actual Twilio integration.
 */
@Component
@Slf4j
public class SmsChannelSender implements ChannelSender {

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(String recipient, String subject, String body) throws ChannelException {
        // TODO: Implement Twilio SMS sending
        // For now, log and throw to trigger fallback
        log.warn("SMS sending not implemented yet. Recipient: {}, Message: {}", recipient, subject);
        throw new ChannelException(NotificationChannel.SMS,
                "SMS channel not yet implemented - falling back to email");
    }
}
