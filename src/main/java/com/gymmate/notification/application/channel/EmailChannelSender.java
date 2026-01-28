package com.gymmate.notification.application.channel;

import com.gymmate.notification.domain.NotificationChannel;
import com.gymmate.shared.service.EmailService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Email channel sender using EmailService.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class EmailChannelSender implements ChannelSender {

    private final EmailService emailService;

    @Override
    public NotificationChannel getChannel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(String recipient, String subject, String body) throws ChannelException {
        try {
            log.debug("Sending email to: {} with subject: {}", recipient, subject);
            emailService.sendHtmlEmail(recipient, subject, body);
        } catch (Exception e) {
            log.error("Failed to send email to {}: {}", recipient, e.getMessage());
            throw new ChannelException(NotificationChannel.EMAIL, "Failed to send email: " + e.getMessage(), e);
        }
    }
}
