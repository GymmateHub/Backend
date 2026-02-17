package com.gymmate.shared.service;

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

@Service
@Slf4j
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender emailSender;
    private final TemplateEngine templateEngine;

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

    public void sendOtpEmail(String to, String firstName, String otp, int validityMinutes) {
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
        } catch (MessagingException e) {
            log.error("Failed to send OTP email to: {}", to, e);
            throw new RuntimeException("Failed to send OTP email", e);
        } catch (Exception e) {
            log.error("Unexpected error sending OTP email to: {}", to, e);
            throw new RuntimeException("Failed to send OTP email", e);
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
     * Send an invite email to a new team member.
     *
     * @param to          recipient email address
     * @param firstName   recipient's first name (or "there" if not provided)
     * @param gymName     name of the gym they're being invited to
     * @param role        the role they're being invited as (ADMIN, TRAINER, STAFF)
     * @param inviterName name of the person who sent the invite
     * @param inviteLink  full URL to accept the invite
     * @param expiryHours number of hours until the invite expires
     */
    @Async
    public void sendInviteEmail(String to, String firstName, String gymName, String role,
                                 String inviterName, String inviteLink, int expiryHours) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("gymName", gymName);
            context.setVariable("role", formatRole(role));
            context.setVariable("inviterName", inviterName);
            context.setVariable("inviteLink", inviteLink);
            context.setVariable("expiryHours", expiryHours);

            String emailContent = templateEngine.process("team-invite", context);

            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("You're invited to join " + gymName + " on GymMate!");
            helper.setText(emailContent, true);

            emailSender.send(message);
            log.info("Invite email sent to: {} for gym: {}", to, gymName);
        } catch (MessagingException e) {
            log.error("Failed to send invite email to: {}", to, e);
            throw new RuntimeException("Failed to send invite email", e);
        }
    }

    /**
     * Format role name for display (ADMIN -> Admin, TRAINER -> Trainer, etc.)
     */
    private String formatRole(String role) {
        if (role == null || role.isEmpty()) {
            return "Team Member";
        }
        return role.charAt(0) + role.substring(1).toLowerCase();
    }
}
