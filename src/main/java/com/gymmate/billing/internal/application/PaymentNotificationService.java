package com.gymmate.billing.internal.application;

import com.gymmate.billing.internal.domain.GymPaymentAccount;
import com.gymmate.billing.internal.application.port.GymPaymentAccountPort;
import com.gymmate.billing.internal.application.port.OrganisationBillingInfoProvider;
import com.gymmate.billing.internal.domain.GymInvoice;
import com.gymmate.configuration.api.MessageTemplateConfigApi;
import com.gymmate.configuration.api.dto.RenderedEmail;
import com.gymmate.shared.multitenancy.TenantScope;
import com.gymmate.billing.internal.domain.Subscription;
import com.gymmate.identity.api.dto.UserSummary;
import com.gymmate.identity.api.IdentityApi;
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
import java.util.Map;
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
    private final GymPaymentAccountPort gymAccounts;
    private final OrganisationBillingInfoProvider organisationBillingInfoProvider;
    private final IdentityApi identityApi;
    private final MessageTemplateConfigApi messageTemplateConfigApi;

    @Value("${spring.mail.from:noreply@gymmatehub.com}")
    private String fromEmail;

    @Value("${app.email.configuration-set:}")
    private String configurationSet;

    @Value("${app.frontend-url:http://localhost:3000}")
    private String frontendUrl;

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("MMMM d, yyyy");

    /**
     * Send trial ending reminder (3 days before trial ends).
     */
    @Async
    public void sendTrialEndingReminder(UUID organisationId, Subscription subscription) {
        try (TenantScope ignored = TenantScope.activate(organisationId, null)) {
            // Resolve the organisation owner's email for billing notifications
            String recipientEmail = resolveOrganisationOwnerEmail(organisationId);
            if (recipientEmail == null) return;

            // Fall back to gym name for display if available
            String orgName = organisationBillingInfoProvider.findBillingInfo(organisationId)
                    .map(OrganisationBillingInfoProvider.BillingInfo::name)
                    .orElse("Your Gym");

            String tierName = subscription.getTier().getDisplayName();
            BigDecimal price = subscription.getTier().getPrice().setScale(2, RoundingMode.HALF_UP);
            LocalDateTime trialEnd = subscription.getTrialEnd();

            RenderedEmail rendered = messageTemplateConfigApi.renderEmail(
                    "EMAIL_BILLING_TRIAL_ENDING_REMINDER",
                    organisationId,
                    Map.of(
                            "gymName", orgName,
                            "tierName", tierName,
                            "price", price,
                            "trialEndDate", trialEnd.format(DATE_FORMAT),
                            "frontendUrl", frontendUrl));
            if (!rendered.enabled()) return;

            sendEmail(recipientEmail, rendered.subject(), rendered.htmlBody());
            log.info("Sent trial ending reminder to organisation {} ({})", organisationId, recipientEmail);
        }
    }

    /**
     * Send payment success notification.
     */
    @Async
    public void sendPaymentSuccessNotification(UUID gymId, GymInvoice invoice) {
        GymPaymentAccount gym = getGym(gymId);
        if (gym == null) return;

        try (TenantScope ignored = TenantScope.activate(gym.getOrganisationId(), gymId)) {
            BigDecimal scaledAmount = invoice.getAmount().setScale(2, RoundingMode.HALF_UP);
            String invoiceLink = invoice.getHostedInvoiceUrl() != null
                    ? "<a href=\"" + invoice.getHostedInvoiceUrl() + "\">View Invoice</a>"
                    : "";
            String pdfLink = invoice.getInvoicePdfUrl() != null
                    ? " | <a href=\"" + invoice.getInvoicePdfUrl() + "\">Download PDF</a>"
                    : "";

            RenderedEmail rendered = messageTemplateConfigApi.renderEmail(
                    "EMAIL_BILLING_PAYMENT_SUCCESS",
                    gym.getOrganisationId(),
                    Map.of(
                            "gymName", gym.getName(),
                            "amount", scaledAmount,
                            "invoiceNumber", invoice.getInvoiceNumber() != null ? invoice.getInvoiceNumber() : "N/A",
                            "nextBillingDate", invoice.getPeriodEnd() != null ? invoice.getPeriodEnd().format(DATE_FORMAT) : "N/A",
                            "invoiceUrl", invoice.getHostedInvoiceUrl() != null ? invoice.getHostedInvoiceUrl() : "",
                            "pdfUrl", invoice.getInvoicePdfUrl() != null ? invoice.getInvoicePdfUrl() : "",
                            "invoiceLinksHtml", invoiceLink + pdfLink));
            if (!rendered.enabled()) return;

            sendEmail(gym.getContactEmail(), rendered.subject(), rendered.htmlBody());
            log.info("Sent payment success notification to gym {} for invoice {}", gymId, invoice.getInvoiceNumber());
        }
    }

    /**
     * Send payment failed notification.
     */
    @Async
    public void sendPaymentFailedNotification(UUID gymId, BigDecimal amount, String failureReason,
                                               LocalDateTime nextRetryDate) {
        GymPaymentAccount gym = getGym(gymId);
        if (gym == null) return;

        try (TenantScope ignored = TenantScope.activate(gym.getOrganisationId(), gymId)) {
            RenderedEmail rendered = messageTemplateConfigApi.renderEmail(
                    "EMAIL_BILLING_PAYMENT_FAILED",
                    gym.getOrganisationId(),
                    Map.of(
                            "gymName", gym.getName(),
                            "amount", amount.setScale(2, RoundingMode.HALF_UP),
                            "failureReason", failureReason != null ? failureReason : "Unknown error",
                            "nextRetryDate", nextRetryDate != null ? nextRetryDate.format(DATE_FORMAT) : "soon",
                            "frontendUrl", frontendUrl));
            if (!rendered.enabled()) return;

            sendEmail(gym.getContactEmail(), rendered.subject(), rendered.htmlBody());
            log.info("Sent payment failed notification to gym {}", gymId);
        }
    }

    /**
     * Send payment failed notification for an organisation-level (platform
     * subscription) failure, where there is no single gym to address the email to —
     * see the gymId-is-null case in StripeWebhookService.handleInvoicePaymentFailed.
     */
    @Async
    public void sendPaymentFailedNotificationForOrganisation(UUID organisationId, BigDecimal amount,
                                                               String failureReason, LocalDateTime nextRetryDate) {
        try (TenantScope ignored = TenantScope.activate(organisationId, null)) {
            String recipientEmail = resolveOrganisationOwnerEmail(organisationId);
            if (recipientEmail == null) return;

            String orgName = organisationBillingInfoProvider.findBillingInfo(organisationId)
                    .map(OrganisationBillingInfoProvider.BillingInfo::name)
                    .orElse("Your Gym");

            RenderedEmail rendered = messageTemplateConfigApi.renderEmail(
                    "EMAIL_BILLING_PAYMENT_FAILED",
                    organisationId,
                    Map.of(
                            "gymName", orgName,
                            "amount", amount.setScale(2, RoundingMode.HALF_UP),
                            "failureReason", failureReason != null ? failureReason : "Unknown error",
                            "nextRetryDate", nextRetryDate != null ? nextRetryDate.format(DATE_FORMAT) : "soon",
                            "frontendUrl", frontendUrl));
            if (!rendered.enabled()) return;

            sendEmail(recipientEmail, rendered.subject(), rendered.htmlBody());
            log.info("Sent org-level payment failed notification to organisation {} ({})", organisationId, recipientEmail);
        }
    }

    /**
     * Send subscription cancelled notification.
     */
    @Async
    public void sendSubscriptionCancelledNotification(UUID gymId, LocalDateTime accessEndsAt) {
        GymPaymentAccount gym = getGym(gymId);
        if (gym == null) return;

        try (TenantScope ignored = TenantScope.activate(gym.getOrganisationId(), gymId)) {
            RenderedEmail rendered = messageTemplateConfigApi.renderEmail(
                    "EMAIL_BILLING_SUBSCRIPTION_CANCELLED",
                    gym.getOrganisationId(),
                    Map.of(
                            "gymName", gym.getName(),
                            "accessEndsAt", accessEndsAt.format(DATE_FORMAT),
                            "frontendUrl", frontendUrl));
            if (!rendered.enabled()) return;

            sendEmail(gym.getContactEmail(), rendered.subject(), rendered.htmlBody());
            log.info("Sent subscription cancelled notification to gym {}", gymId);
        }
    }

    /**
     * Send subscription reactivated notification.
     */
    @Async
    public void sendSubscriptionReactivatedNotification(UUID gymId, String tierName) {
        GymPaymentAccount gym = getGym(gymId);
        if (gym == null) return;

        try (TenantScope ignored = TenantScope.activate(gym.getOrganisationId(), gymId)) {
            RenderedEmail rendered = messageTemplateConfigApi.renderEmail(
                    "EMAIL_BILLING_SUBSCRIPTION_REACTIVATED",
                    gym.getOrganisationId(),
                    Map.of(
                            "gymName", gym.getName(),
                            "tierName", tierName,
                            "frontendUrl", frontendUrl));
            if (!rendered.enabled()) return;

            sendEmail(gym.getContactEmail(), rendered.subject(), rendered.htmlBody());
            log.info("Sent subscription reactivated notification to gym {}", gymId);
        }
    }

    /**
     * Send welcome email after trial starts.
     */
    @Async
    public void sendTrialStartedEmail(UUID gymId, String tierName, LocalDateTime trialEnd) {
        GymPaymentAccount gym = getGym(gymId);
        if (gym == null) return;

        try (TenantScope ignored = TenantScope.activate(gym.getOrganisationId(), gymId)) {
            RenderedEmail rendered = messageTemplateConfigApi.renderEmail(
                    "EMAIL_BILLING_TRIAL_STARTED",
                    gym.getOrganisationId(),
                    Map.of(
                            "gymName", gym.getName(),
                            "tierName", tierName,
                            "trialEndDate", trialEnd.format(DATE_FORMAT),
                            "frontendUrl", frontendUrl));
            if (!rendered.enabled()) return;

            sendEmail(gym.getContactEmail(), rendered.subject(), rendered.htmlBody());
            log.info("Sent trial started email to gym {}", gymId);
        }
    }

    // Private helper methods

    private GymPaymentAccount getGym(UUID gymId) {
        return gymAccounts.findById(gymId).orElse(null);
    }

    /**
     * Resolve the organisation owner's email for billing/payment notifications.
     * Priority: owner user email → org billing email → org contact email.
     */
    private String resolveOrganisationOwnerEmail(UUID organisationId) {
        OrganisationBillingInfoProvider.BillingInfo org = organisationBillingInfoProvider.findBillingInfo(organisationId).orElse(null);
        if (org == null) {
            log.warn("Organisation {} not found, cannot send notification", organisationId);
            return null;
        }

        // 1. Try the owner user's email
        if (org.ownerUserId() != null) {
            String ownerEmail = identityApi.findUser(org.ownerUserId())
                    .map(UserSummary::email)
                    .orElse(null);
            if (ownerEmail != null && !ownerEmail.isBlank()) {
                return ownerEmail;
            }
        }

        // 2. Fall back to org billing email
        if (org.billingEmail() != null && !org.billingEmail().isBlank()) {
            return org.billingEmail();
        }

        // 3. Fall back to org contact email
        if (org.contactEmail() != null && !org.contactEmail().isBlank()) {
            return org.contactEmail();
        }

        log.warn("No email found for organisation {}", organisationId);
        return null;
    }

    private void sendEmail(String to, String subject, String htmlContent) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(htmlContent, true);

            if (org.springframework.util.StringUtils.hasText(configurationSet)) {
                message.setHeader("X-SES-CONFIGURATION-SET", configurationSet);
                message.setHeader("X-SES-MESSAGE-TAGS", "app=gymmatehub,type=payment");
            }
            message.setHeader("X-SES-TENANT", "gymmatehub");

            emailSender.send(message);
        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}", to, e.getMessage());
        }
    }
}

