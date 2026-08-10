package com.gymmate.whitelabel.application;

import com.gymmate.shared.exception.DomainException;
import com.gymmate.whitelabel.domain.SmtpSecurity;
import com.gymmate.whitelabel.domain.WhitelabelSettings;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.util.Map;
import java.util.Properties;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Factory for creating dynamic, tenant-specific JavaMailSender instances
 * using the Organisation or Gym's configured SMTP settings.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DynamicMailSenderFactory {

    private final WhitelabelEncryptionService encryptionService;
    private final Map<String, JavaMailSender> senderCache = new ConcurrentHashMap<>();

    /**
     * Get or build a JavaMailSender for the provided WhitelabelSettings.
     * Throws DomainException if SMTP is not enabled or configured for the tenant.
     */
    public JavaMailSender getMailSender(WhitelabelSettings settings) {
        if (settings == null || !settings.isSmtpEnabled() || !StringUtils.hasText(settings.getSmtpHost())) {
            throw new DomainException("SMTP_NOT_CONFIGURED",
                    "Custom SMTP settings are not configured or enabled for this tenant.");
        }

        String cacheKey = getCacheKey(settings);
        return senderCache.computeIfAbsent(cacheKey, key -> buildMailSender(settings));
    }

    /**
     * Clear cached sender when settings are updated.
     */
    public void evictCache(UUID organisationId, UUID gymId) {
        String prefix = organisationId + ":" + (gymId != null ? gymId : "org");
        senderCache.keySet().removeIf(k -> k.startsWith(prefix));
    }

    /**
     * Test connection and send a test email using raw SMTP request parameters.
     */
    public void testSmtpConnection(
            String host,
            Integer port,
            String username,
            String password,
            SmtpSecurity security,
            String fromEmail,
            String recipientEmail) {

        if (!StringUtils.hasText(host)) {
            throw new DomainException("INVALID_SMTP_CONFIG", "SMTP Host is required");
        }

        int smtpPort = (port != null && port > 0) ? port : (security == SmtpSecurity.SSL ? 465 : 587);
        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(host);
        sender.setPort(smtpPort);

        if (StringUtils.hasText(username)) {
            sender.setUsername(username);
        }
        if (StringUtils.hasText(password)) {
            sender.setPassword(password);
        }

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(StringUtils.hasText(username)));
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        if (security == SmtpSecurity.SSL) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", String.valueOf(smtpPort));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else if (security == SmtpSecurity.STARTTLS) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        try {
            MimeMessage message = sender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            String senderAddr = StringUtils.hasText(fromEmail) ? fromEmail : "noreply@gymmate.com";

            helper.setFrom(senderAddr);
            helper.setTo(recipientEmail);
            helper.setSubject("GymMate Whitelabel SMTP Connection Test");
            helper.setText("<h3>SMTP Connection Test</h3><p>Your custom SMTP settings have been validated successfully!</p>", true);

            sender.send(message);
            log.info("Test SMTP email sent successfully to {}", recipientEmail);
        } catch (Exception e) {
            log.error("Failed SMTP connection test to {}:{}", host, smtpPort, e);
            throw new DomainException("SMTP_TEST_FAILED", "Failed to send test email: " + e.getMessage());
        }
    }

    private JavaMailSender buildMailSender(WhitelabelSettings settings) {
        int smtpPort = (settings.getSmtpPort() != null && settings.getSmtpPort() > 0)
                ? settings.getSmtpPort()
                : (settings.getSmtpSecurity() == SmtpSecurity.SSL ? 465 : 587);

        JavaMailSenderImpl sender = new JavaMailSenderImpl();
        sender.setHost(settings.getSmtpHost());
        sender.setPort(smtpPort);

        if (StringUtils.hasText(settings.getSmtpUsername())) {
            sender.setUsername(settings.getSmtpUsername());
        }

        if (StringUtils.hasText(settings.getSmtpPasswordEncrypted())) {
            String decryptedPassword = encryptionService.decrypt(settings.getSmtpPasswordEncrypted());
            sender.setPassword(decryptedPassword);
        }

        Properties props = sender.getJavaMailProperties();
        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", String.valueOf(StringUtils.hasText(settings.getSmtpUsername())));
        props.put("mail.smtp.connectiontimeout", "5000");
        props.put("mail.smtp.timeout", "5000");

        SmtpSecurity security = settings.getSmtpSecurity() != null ? settings.getSmtpSecurity() : SmtpSecurity.STARTTLS;
        if (security == SmtpSecurity.SSL) {
            props.put("mail.smtp.ssl.enable", "true");
            props.put("mail.smtp.socketFactory.port", String.valueOf(smtpPort));
            props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");
        } else if (security == SmtpSecurity.STARTTLS) {
            props.put("mail.smtp.starttls.enable", "true");
        }

        return sender;
    }

    private String getCacheKey(WhitelabelSettings settings) {
        return settings.getOrganisationId() + ":" + (settings.getGymId() != null ? settings.getGymId() : "org")
                + ":" + settings.getSmtpHost() + ":" + settings.getSmtpPort() + ":" + settings.getUpdatedAt();
    }
}
