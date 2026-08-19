package com.gymmate.notification.application;

import com.gymmate.notification.infrastructure.SseEmitterRegistry;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.whitelabel.application.DynamicMailSenderFactory;
import com.gymmate.whitelabel.application.WhitelabelSettingsService;
import com.gymmate.whitelabel.domain.WhitelabelSettings;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final TemplateEngine templateEngine;
    private final SseEmitterRegistry sseEmitterRegistry;
    private final WhitelabelSettingsService whitelabelSettingsService;
    private final DynamicMailSenderFactory mailSenderFactory;
    private final EmailSuppressionService suppressionService;
    private final com.gymmate.notification.application.port.SesTenantResolver sesTenantResolver;
    private final com.gymmate.notification.application.port.SesConfigurationSetResolver sesConfigurationSetResolver;
    // System default sender, autoconfigured by Spring Boot from spring.mail.* (application-email-config.yml).
    // Used whenever the tenant has no whitelabel SMTP configured/enabled — e.g. every /api/auth/**
    // request, where TenantContext is never populated (see TenantFilter.NON_TENANT_ENDPOINTS), so a
    // whitelabel lookup is guaranteed to come back empty. BUG-001.
    private final JavaMailSender defaultMailSender;

    @Value("${spring.mail.from:noreply@gymmatehub.com}")
    private String defaultFromEmail;

    @Async
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("resetLink", resetLink);

            String emailContent = templateEngine.process("password-reset", context);
            sendEmailInternal(to, "Reset Your Password", emailContent);
            log.info("Password reset email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Async
    public void sendOtpEmail(String to, String firstName, String otp, int validityMinutes, String userId) {
        sseEmitterRegistry.sendEmailStatus(userId, "SENDING", "Sending verification email...");

        try {
            log.info("Attempting to send OTP email to: {} with OTP: {}", to, otp);

            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("otp", otp);
            context.setVariable("validityMinutes", validityMinutes);

            String emailContent = templateEngine.process("registration-otp", context);
            sendEmailInternal(to, "Your Verification Code", emailContent);
            log.info("OTP email sent successfully to: {}", to);

            sseEmitterRegistry.sendEmailStatus(userId, "SENT", "Verification email sent successfully");

        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", to, e);
            sseEmitterRegistry.sendEmailStatus(userId, "FAILED",
                    "Failed to send verification email. Please try resending.");
        }
    }

    @Async
    public void sendWelcomeEmail(String to, String firstName) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);

            String emailContent = templateEngine.process("welcome", context);
            sendEmailInternal(to, "Welcome!", emailContent);
            log.info("Welcome email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", to, e);
            log.warn("Continuing despite welcome email failure");
        }
    }

    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            sendEmailInternal(to, subject, htmlBody);
            log.info("HTML email sent to: {} with subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    @Async
    public void sendSubscriptionRenewalEmail(String to, String organisationName, String planName,
            LocalDate renewalDate, BigDecimal amount) {
        try {
            Context context = new Context();
            context.setVariable("organisationName", organisationName);
            context.setVariable("planName", planName);
            context.setVariable("renewalDate", renewalDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));
            context.setVariable("amount", amount);

            String emailContent = templateEngine.process("subscription-renewal", context);
            sendEmailInternal(to, "Subscription Renewal Notice", emailContent);
            log.info("Subscription renewal email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send subscription renewal email to: {}", to, e);
        }
    }

    @Async
    public void sendTrialEndingEmail(String to, String organisationName, LocalDate trialEndDate) {
        try {
            Context context = new Context();
            context.setVariable("organisationName", organisationName);
            context.setVariable("trialEndDate", trialEndDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

            String emailContent = templateEngine.process("subscription-trial-ending", context);
            sendEmailInternal(to, "Your Trial is Ending Soon", emailContent);
            log.info("Trial ending email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send trial ending email to: {}", to, e);
        }
    }

    @Async
    public void sendSubscriptionExpiredEmail(String to, String organisationName, LocalDate expiryDate) {
        try {
            Context context = new Context();
            context.setVariable("organisationName", organisationName);
            context.setVariable("expiryDate", expiryDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy")));

            String emailContent = templateEngine.process("subscription-expired", context);
            sendEmailInternal(to, "Subscription Expired", emailContent);
            log.info("Subscription expired email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send subscription expired email to: {}", to, e);
        }
    }

    @Async
    public void sendMarketingHtmlEmail(String to, String subject, String htmlBody, String unsubscribeUrl) {
        try {
            sendEmailInternal(to, subject, htmlBody, unsubscribeUrl);
            log.info("Marketing HTML email sent to: {} with subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send marketing HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send marketing HTML email", e);
        }
    }

    private void sendEmailInternal(String to, String subject, String content) throws MessagingException {
        sendEmailInternal(to, subject, content, null);
    }

    private void sendEmailInternal(String to, String subject, String content, String unsubscribeUrl) throws MessagingException {
        // Enforce deliverability check: skip send if recipient is actively suppressed
        if (suppressionService.isSuppressed(to)) {
            log.warn("Suppressed email recipient detected [{}]. Aborting outbound send for subject: {}", to, subject);
            return;
        }

        UUID organisationId = TenantContext.getCurrentTenantId();
        UUID gymId = TenantContext.getCurrentGymId();

        Optional<WhitelabelSettings> whitelabelOpt = whitelabelSettingsService.getWhitelabelSettings(organisationId, gymId);

        JavaMailSender mailSender;
        String from;

        if (whitelabelOpt.isPresent() && whitelabelOpt.get().isSmtpEnabled()) {
            WhitelabelSettings settings = whitelabelOpt.get();
            mailSender = mailSenderFactory.getMailSender(settings);

            from = StringUtils.hasText(settings.getSmtpFromEmail())
                    ? settings.getSmtpFromEmail()
                    : (StringUtils.hasText(settings.getSmtpUsername()) ? settings.getSmtpUsername() : defaultFromEmail);

            if (StringUtils.hasText(settings.getSmtpFromName())) {
                from = settings.getSmtpFromName() + " <" + from + ">";
            } else if (StringUtils.hasText(settings.getBrandName())) {
                from = settings.getBrandName() + " <" + from + ">";
            }
        } else {
            // No tenant custom SMTP (or none applicable, e.g. unauthenticated /api/auth/** requests) —
            // fall back to the system default sender instead of failing the request. BUG-001.
            mailSender = defaultMailSender;
            from = defaultFromEmail;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        // Attach AWS SES Tenant, Configuration Set, and tag headers when using system sender
        if (mailSender == defaultMailSender) {
            String sesTenant = sesTenantResolver.resolveTenant(organisationId, gymId);
            if (StringUtils.hasText(sesTenant)) {
                message.setHeader("X-SES-TENANT", sesTenant);
            }

            String sesConfigSet = sesConfigurationSetResolver.resolveConfigurationSet(organisationId, gymId);
            if (StringUtils.hasText(sesConfigSet)) {
                message.setHeader("X-SES-CONFIGURATION-SET", sesConfigSet);
            }

            message.setHeader("X-SES-MESSAGE-TAGS", "app=" + (StringUtils.hasText(sesTenant) ? sesTenant : "gymmatehub") + ",type=transactional");
        }

        // Attach RFC 8058 one-click unsubscribe headers if an unsubscribe link is supplied
        if (StringUtils.hasText(unsubscribeUrl)) {
            message.setHeader("List-Unsubscribe", "<" + unsubscribeUrl + ">, <mailto:unsubscribe@gymmatehub.com>");
            message.setHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");
        }

        mailSender.send(message);
    }
}
