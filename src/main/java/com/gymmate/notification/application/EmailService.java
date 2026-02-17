package com.gymmate.notification.application;

import com.gymmate.notification.infrastructure.SseEmitterRegistry;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;
    private final SseEmitterRegistry sseEmitterRegistry;

    @Value("${spring.mail.from:no-reply@gymmatehub.com}")
    private String fromEmail;

    @Async
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("resetLink", resetLink);

            String emailContent = templateEngine.process("password-reset", context);

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Reset Your Password");
            helper.setText(emailContent, true);

            emailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Async
    public void sendOtpEmail(String to, String firstName, String otp, int validityMinutes, String userId) {
        // Notify client that email is being sent
        sseEmitterRegistry.sendEmailStatus(userId, "SENDING", "Sending verification email...");

        try {
            log.info("Attempting to send OTP email to: {} with OTP: {}", to, otp);

            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("otp", otp);
            context.setVariable("validityMinutes", validityMinutes);

            String emailContent = templateEngine.process("registration-otp", context);

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Your GymMateHub Verification Code");
            helper.setText(emailContent, true);

            emailSender.send(message);
            log.info("OTP email sent successfully to: {}", to);

            // Notify client of success
            sseEmitterRegistry.sendEmailStatus(userId, "SENT", "Verification email sent successfully");

        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", to, e);

            // Notify client of failure — don't throw, the OTP is already stored
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

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Welcome to GymMate!");
            helper.setText(emailContent, true);

            emailSender.send(message);
            log.info("Welcome email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", to, e);
            // Don't throw exception for welcome email - it's not critical
            log.warn("Continuing despite welcome email failure");
        }
    }

    /**
     * Send a generic HTML email with custom subject and body.
     * Used for newsletters and other custom emails.
     *
     * @param to       recipient email address
     * @param subject  email subject line
     * @param htmlBody HTML content for the email body
     */
    @Async
    public void sendHtmlEmail(String to, String subject, String htmlBody) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            emailSender.send(message);
            log.info("HTML email sent to: {} with subject: {}", to, subject);
        } catch (MessagingException e) {
            log.error("Failed to send HTML email to: {}", to, e);
            throw new RuntimeException("Failed to send HTML email", e);
        }
    }

    /**
     * Send subscription renewal reminder email.
     */
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

    /**
     * Send trial ending soon email.
     */
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

    /**
     * Send subscription expired email.
     */
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
        MimeMessage message = emailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(fromEmail);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);

        emailSender.send(message);
    }
}
