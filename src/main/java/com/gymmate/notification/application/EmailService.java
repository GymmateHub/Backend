package com.gymmate.notification.application;

import com.gymmate.notification.infrastructure.SseEmitterRegistry;
import com.gymmate.shared.exception.DomainException;
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

    private void sendEmailInternal(String to, String subject, String content) throws MessagingException {
        UUID organisationId = TenantContext.getCurrentTenantId();
        UUID gymId = TenantContext.getCurrentGymId();

        Optional<WhitelabelSettings> whitelabelOpt = whitelabelSettingsService.getWhitelabelSettings(organisationId, gymId);

        if (whitelabelOpt.isEmpty() || !whitelabelOpt.get().isSmtpEnabled()) {
            throw new DomainException("SMTP_NOT_CONFIGURED",
                    "Custom SMTP configuration is missing or disabled for tenant: " + organisationId);
        }

        WhitelabelSettings settings = whitelabelOpt.get();
        JavaMailSender mailSender = mailSenderFactory.getMailSender(settings);

        String from = StringUtils.hasText(settings.getSmtpFromEmail())
                ? settings.getSmtpFromEmail()
                : (StringUtils.hasText(settings.getSmtpUsername()) ? settings.getSmtpUsername() : defaultFromEmail);

        if (StringUtils.hasText(settings.getSmtpFromName())) {
            from = settings.getSmtpFromName() + " <" + from + ">";
        } else if (StringUtils.hasText(settings.getBrandName())) {
            from = settings.getBrandName() + " <" + from + ">";
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        mailSender.send(message);
    }
}
