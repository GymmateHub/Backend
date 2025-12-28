package com.gymmate.payment.application;

import com.gymmate.Gym.domain.Gym;
import com.gymmate.Gym.infrastructure.GymRepository;
import com.gymmate.payment.domain.GymInvoice;
import com.gymmate.subscription.domain.GymSubscription;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

/**
 * Service for sending payment-related email notifications.
 * Handles trial reminders, payment success/failure notifications, and invoice emails.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PaymentNotificationService {

    private final JavaMailSender emailSender;
    private final GymRepository gymRepository;

    @Value("${spring.mail.from:no-reply@gymmatehub.com}")
    private String fromEmail;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    /**
     * Send trial ending reminder (3 days before trial ends).
     */
    @Async
    public void sendTrialEndingReminder(UUID gymId, GymSubscription subscription) {
        Gym gym = getGym(gymId);
        if (gym == null) return;

        String subject = "Your GymMate Trial Ends in 3 Days";
        String tierName = subscription.getTier().getDisplayName();
        BigDecimal price = subscription.getTier().getPrice();
        LocalDateTime trialEnd = subscription.getTrialEnd();

        String htmlContent = buildTrialEndingEmail(gym.getName(), tierName, price, trialEnd);

        sendEmail(gym.getContactEmail(), subject, htmlContent);
        log.info("Sent trial ending reminder to gym {}", gymId);
    }

    /**
     * Send payment success notification.
     */
    @Async
    public void sendPaymentSuccessNotification(UUID gymId, GymInvoice invoice) {
        Gym gym = getGym(gymId);
        if (gym == null) return;

        String subject = "Payment Received - $" + invoice.getAmount().setScale(2, RoundingMode.HALF_UP);
        String htmlContent = buildPaymentSuccessEmail(
            gym.getName(),
            invoice.getAmount(),
            invoice.getInvoiceNumber(),
            invoice.getPeriodEnd(),
            invoice.getHostedInvoiceUrl(),
            invoice.getInvoicePdfUrl()
        );

        sendEmail(gym.getContactEmail(), subject, htmlContent);
        log.info("Sent payment success notification to gym {} for invoice {}", gymId, invoice.getInvoiceNumber());
    }

    /**
     * Send payment failed notification.
     */
    @Async
    public void sendPaymentFailedNotification(UUID gymId, BigDecimal amount, String failureReason,
                                               LocalDateTime nextRetryDate) {
        Gym gym = getGym(gymId);
        if (gym == null) return;

        String subject = "⚠️ Payment Failed - Action Required";
        String htmlContent = buildPaymentFailedEmail(gym.getName(), amount, failureReason, nextRetryDate);

        sendEmail(gym.getContactEmail(), subject, htmlContent);
        log.info("Sent payment failed notification to gym {}", gymId);
    }

    /**
     * Send subscription cancelled notification.
     */
    @Async
    public void sendSubscriptionCancelledNotification(UUID gymId, LocalDateTime accessEndsAt) {
        Gym gym = getGym(gymId);
        if (gym == null) return;

        String subject = "Your GymMate Subscription Has Been Cancelled";
        String htmlContent = buildSubscriptionCancelledEmail(gym.getName(), accessEndsAt);

        sendEmail(gym.getContactEmail(), subject, htmlContent);
        log.info("Sent subscription cancelled notification to gym {}", gymId);
    }

    /**
     * Send subscription reactivated notification.
     */
    @Async
    public void sendSubscriptionReactivatedNotification(UUID gymId, String tierName) {
        Gym gym = getGym(gymId);
        if (gym == null) return;

        String subject = "Welcome Back! Your GymMate Subscription is Active";
        String htmlContent = buildSubscriptionReactivatedEmail(gym.getName(), tierName);

        sendEmail(gym.getContactEmail(), subject, htmlContent);
        log.info("Sent subscription reactivated notification to gym {}", gymId);
    }

    /**
     * Send welcome email after trial starts.
     */
    @Async
    public void sendTrialStartedEmail(UUID gymId, String tierName, LocalDateTime trialEnd) {
        Gym gym = getGym(gymId);
        if (gym == null) return;

        String subject = "Welcome to GymMate! Your Free Trial Has Started";
        String htmlContent = buildTrialStartedEmail(gym.getName(), tierName, trialEnd);

        sendEmail(gym.getContactEmail(), subject, htmlContent);
        log.info("Sent trial started email to gym {}", gymId);
    }

    // Private helper methods

    private Gym getGym(UUID gymId) {
        return gymRepository.findById(gymId).orElse(null);
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            emailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }

    private String buildTrialEndingEmail(String gymName, String tierName, BigDecimal price, LocalDateTime trialEnd) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #fff; padding: 30px; border: 1px solid #e0e0e0; border-radius: 0 0 8px 8px; }
                    .cta-button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Your Trial Ends Soon!</h1>
                    </div>
                    <div class="content">
                        <p>Hi %s,</p>
                        <p>Your 14-day free trial will end on <strong>%s</strong>.</p>
                        <p>You're currently on the <strong>%s</strong> plan at <strong>$%s/month</strong>.</p>
                        <p>To continue enjoying GymMate without interruption, no action is needed. Your card on file will be charged automatically.</p>
                        <p>Want to make changes? You can:</p>
                        <ul>
                            <li>Update your payment method</li>
                            <li>Change your subscription plan</li>
                            <li>Cancel before the trial ends</li>
                        </ul>
                        <a href="%s/gym/settings/subscription" class="cta-button">Manage Subscription</a>
                        <p>Questions? Reply to this email or contact our support team.</p>
                        <p>Thanks for trying GymMate!</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 GymMate. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(gymName, trialEnd.format(DATE_FORMAT), tierName, price.setScale(2, RoundingMode.HALF_UP), frontendUrl);
    }

    private String buildPaymentSuccessEmail(String gymName, BigDecimal amount, String invoiceNumber,
                                             LocalDateTime nextBillingDate, String invoiceUrl, String pdfUrl) {
        String invoiceLink = invoiceUrl != null
            ? "<a href=\"" + invoiceUrl + "\">View Invoice</a>"
            : "";
        String pdfLink = pdfUrl != null
            ? " | <a href=\"" + pdfUrl + "\">Download PDF</a>"
            : "";

        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #28a745; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #fff; padding: 30px; border: 1px solid #e0e0e0; border-radius: 0 0 8px 8px; }
                    .amount { font-size: 36px; font-weight: bold; color: #28a745; }
                    .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>✅ Payment Received</h1>
                    </div>
                    <div class="content">
                        <p>Hi %s,</p>
                        <p>Thank you for your payment!</p>
                        <p class="amount">$%s</p>
                        <p><strong>Invoice:</strong> %s</p>
                        <p><strong>Next billing date:</strong> %s</p>
                        <p>%s%s</p>
                        <p>Thank you for being a GymMate customer!</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 GymMate. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(gymName, amount.setScale(2, RoundingMode.HALF_UP),
                invoiceNumber != null ? invoiceNumber : "N/A",
                nextBillingDate != null ? nextBillingDate.format(DATE_FORMAT) : "N/A",
                invoiceLink, pdfLink);
    }

    private String buildPaymentFailedEmail(String gymName, BigDecimal amount, String failureReason,
                                            LocalDateTime nextRetryDate) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #dc3545; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #fff; padding: 30px; border: 1px solid #e0e0e0; border-radius: 0 0 8px 8px; }
                    .cta-button { display: inline-block; background: #dc3545; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .warning { background: #fff3cd; border: 1px solid #ffc107; padding: 15px; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>⚠️ Payment Failed</h1>
                    </div>
                    <div class="content">
                        <p>Hi %s,</p>
                        <p>We were unable to process your payment of <strong>$%s</strong>.</p>
                        <div class="warning">
                            <strong>Reason:</strong> %s
                        </div>
                        <p>We will automatically retry on <strong>%s</strong>.</p>
                        <p>To avoid service interruption, please update your payment method:</p>
                        <a href="%s/gym/settings/payments" class="cta-button">Update Payment Method</a>
                        <p>Need help? Contact our support team.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 GymMate. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(gymName, amount.setScale(2, RoundingMode.HALF_UP),
                failureReason != null ? failureReason : "Unknown error",
                nextRetryDate != null ? nextRetryDate.format(DATE_FORMAT) : "soon",
                frontendUrl);
    }

    private String buildSubscriptionCancelledEmail(String gymName, LocalDateTime accessEndsAt) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: #6c757d; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #fff; padding: 30px; border: 1px solid #e0e0e0; border-radius: 0 0 8px 8px; }
                    .cta-button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Subscription Cancelled</h1>
                    </div>
                    <div class="content">
                        <p>Hi %s,</p>
                        <p>Your GymMate subscription has been cancelled.</p>
                        <p>You'll continue to have access until <strong>%s</strong>.</p>
                        <p>We're sorry to see you go! If you change your mind, you can reactivate your subscription anytime:</p>
                        <a href="%s/gym/settings/subscription" class="cta-button">Reactivate Subscription</a>
                        <p>We'd love to hear your feedback on how we can improve.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 GymMate. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(gymName, accessEndsAt.format(DATE_FORMAT), frontendUrl);
    }

    private String buildSubscriptionReactivatedEmail(String gymName, String tierName) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #fff; padding: 30px; border: 1px solid #e0e0e0; border-radius: 0 0 8px 8px; }
                    .cta-button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Welcome Back!</h1>
                    </div>
                    <div class="content">
                        <p>Hi %s,</p>
                        <p>Great news! Your GymMate subscription has been reactivated.</p>
                        <p>You're now on the <strong>%s</strong> plan with full access to all features.</p>
                        <a href="%s/gym/dashboard" class="cta-button">Go to Dashboard</a>
                        <p>Thank you for choosing GymMate!</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 GymMate. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(gymName, tierName, frontendUrl);
    }

    private String buildTrialStartedEmail(String gymName, String tierName, LocalDateTime trialEnd) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background: linear-gradient(135deg, #667eea 0%%, #764ba2 100%%); color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                    .content { background: #fff; padding: 30px; border: 1px solid #e0e0e0; border-radius: 0 0 8px 8px; }
                    .cta-button { display: inline-block; background: #667eea; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                    .feature-list { background: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0; }
                    .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>🎉 Welcome to GymMate!</h1>
                    </div>
                    <div class="content">
                        <p>Hi %s,</p>
                        <p>Your 14-day free trial of the <strong>%s</strong> plan has started!</p>
                        <p>Trial ends on: <strong>%s</strong></p>
                        <div class="feature-list">
                            <strong>Here's what you can do:</strong>
                            <ul>
                                <li>Add and manage gym members</li>
                                <li>Create and schedule classes</li>
                                <li>Set up membership plans</li>
                                <li>Accept member payments (after Connect setup)</li>
                                <li>Track attendance and analytics</li>
                            </ul>
                        </div>
                        <a href="%s/gym/dashboard" class="cta-button">Start Exploring</a>
                        <p>Need help getting started? Check out our <a href="%s/help">Help Center</a>.</p>
                    </div>
                    <div class="footer">
                        <p>© 2024 GymMate. All rights reserved.</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(gymName, tierName, trialEnd.format(DATE_FORMAT), frontendUrl, frontendUrl);
    }
}

