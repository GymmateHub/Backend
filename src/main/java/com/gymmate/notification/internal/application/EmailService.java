package com.gymmate.notification.internal.application;

import com.gymmate.configuration.api.MessageTemplateConfigApi;
import com.gymmate.configuration.api.dto.RenderedEmail;
import com.gymmate.notification.api.EmailApi;
import com.gymmate.notification.internal.application.port.RealtimeNotifier;
import com.gymmate.shared.multitenancy.TenantContext;
import com.gymmate.whitelabel.api.WhitelabelApi;
import com.gymmate.whitelabel.api.dto.WhitelabelProfile;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.ClassPathResource;
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
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

@Service
@Slf4j
public class EmailService implements EmailApi {

    /**
     * The GymMateHub logo for email headers, sent as an inline (CID) image: email clients don't
     * render SVG and Gmail blocks data: URIs, and an attachment needs no hosted URL. A template
     * opts in by referencing {@code src="cid:gymmatehub-logo"}. The PNG is the white Kettle Gm
     * lockup ({@code gymmatehub-lockup-white.svg} from the GymMateHub brand identity, as used in
     * the frontend and mobile repos) rendered at 2x (522x80) for a 261x40 header.
     */
    static final String LOGO_CONTENT_ID = "gymmatehub-logo";
    private static final ClassPathResource LOGO = new ClassPathResource("email/gymmatehub-logo.png");

    private final TemplateEngine templateEngine;
    private final RealtimeNotifier sseEmitterRegistry;
    private final WhitelabelApi whitelabelApi;
    private final EmailSuppressionService suppressionService;
    private final com.gymmate.notification.internal.application.port.SesTenantResolver sesTenantResolver;
    private final com.gymmate.notification.internal.application.port.SesConfigurationSetResolver sesConfigurationSetResolver;
    // System default sender, autoconfigured by Spring Boot from spring.mail.*
    // (application-email-config.yml).
    // Used whenever the tenant has no whitelabel SMTP configured/enabled — e.g.
    // every /api/auth/**
    // request, where TenantContext is never populated (see
    // TenantFilter.NON_TENANT_ENDPOINTS), so a
    // whitelabel lookup is guaranteed to come back empty. BUG-001.
    private final JavaMailSender defaultMailSender;
    private final MessageTemplateConfigApi messageTemplateConfigApi;

    @Autowired
    public EmailService(
            TemplateEngine templateEngine,
            RealtimeNotifier sseEmitterRegistry,
            WhitelabelApi whitelabelApi,
            EmailSuppressionService suppressionService,
            com.gymmate.notification.internal.application.port.SesTenantResolver sesTenantResolver,
            com.gymmate.notification.internal.application.port.SesConfigurationSetResolver sesConfigurationSetResolver,
            JavaMailSender defaultMailSender,
            MessageTemplateConfigApi messageTemplateConfigApi) {
        this.templateEngine = templateEngine;
        this.sseEmitterRegistry = sseEmitterRegistry;
        this.whitelabelApi = whitelabelApi;
        this.suppressionService = suppressionService;
        this.sesTenantResolver = sesTenantResolver;
        this.sesConfigurationSetResolver = sesConfigurationSetResolver;
        this.defaultMailSender = defaultMailSender;
        this.messageTemplateConfigApi = messageTemplateConfigApi;
    }

    public EmailService(
            TemplateEngine templateEngine,
            RealtimeNotifier sseEmitterRegistry,
            WhitelabelApi whitelabelApi,
            EmailSuppressionService suppressionService,
            com.gymmate.notification.internal.application.port.SesTenantResolver sesTenantResolver,
            com.gymmate.notification.internal.application.port.SesConfigurationSetResolver sesConfigurationSetResolver,
            JavaMailSender defaultMailSender) {
        this(templateEngine, sseEmitterRegistry, whitelabelApi, suppressionService,
                sesTenantResolver, sesConfigurationSetResolver, defaultMailSender, null);
    }

    @Value("${spring.mail.from:noreply@gymmatehub.com}")
    private String defaultFromEmail;

    @Value("${app.email.configuration-set:}")
    private String configurationSet;

    @Async
    @Override
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        try {
            Context context = new Context();
            context.setVariable("name", name);
            context.setVariable("resetLink", resetLink);

            RenderedEmail configured = resolveTemplate(
                    "EMAIL_PASSWORD_RESET",
                    Map.of("name", name != null ? name : "", "resetLink", resetLink != null ? resetLink : ""));
            if (configured != null && !configured.enabled()) {
                log.info("Skipping disabled email template EMAIL_PASSWORD_RESET for recipient: {}", to);
                return;
            }

            String subject = configured != null && StringUtils.hasText(configured.subject())
                    ? configured.subject()
                    : "Reset Your Password";
            String emailContent = configured != null && configured.bodyCustomised() && StringUtils.hasText(configured.htmlBody())
                    ? configured.htmlBody()
                    : templateEngine.process("password-reset", context);
            sendEmailInternal(to, subject, emailContent);
            log.info("Password reset email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send password reset email to: {}", to, e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }

    @Async
    @Override
    public void sendOtpEmail(String to, String firstName, String otp, int validityMinutes, String userId) {
        sseEmitterRegistry.sendEmailStatus(userId, "SENDING", "Sending verification email...");

        try {
            log.info("Attempting to send OTP email to: {} with OTP: {}", to, otp);

            Context context = new Context();
            context.setVariable("firstName", firstName);
            context.setVariable("otp", otp);
            context.setVariable("validityMinutes", validityMinutes);

            RenderedEmail configured = resolveTemplate(
                    "EMAIL_REGISTRATION_OTP",
                    Map.of(
                            "firstName", firstName != null ? firstName : "",
                            "otp", otp != null ? otp : "",
                            "validityMinutes", validityMinutes));
            if (configured != null && !configured.enabled()) {
                log.info("Skipping disabled email template EMAIL_REGISTRATION_OTP for recipient: {}", to);
                return;
            }

            String subject = configured != null && StringUtils.hasText(configured.subject())
                    ? configured.subject()
                    : "Your Verification Code";
            String emailContent = configured != null && configured.bodyCustomised() && StringUtils.hasText(configured.htmlBody())
                    ? configured.htmlBody()
                    : templateEngine.process("registration-otp", context);
            sendEmailInternal(to, subject, emailContent);
            log.info("OTP email sent successfully to: {}", to);

            sseEmitterRegistry.sendEmailStatus(userId, "SENT", "Verification email sent successfully");

        } catch (Exception e) {
            log.error("Failed to send OTP email to: {}", to, e);
            sseEmitterRegistry.sendEmailStatus(userId, "FAILED",
                    "Failed to send verification email. Please try resending.");
        }
    }

    @Async
    @Override
    public void sendWelcomeEmail(String to, String firstName) {
        try {
            Context context = new Context();
            context.setVariable("firstName", firstName);

            RenderedEmail configured = resolveTemplate(
                    "EMAIL_WELCOME",
                    Map.of("firstName", firstName != null ? firstName : ""));
            if (configured != null && !configured.enabled()) {
                log.info("Skipping disabled email template EMAIL_WELCOME for recipient: {}", to);
                return;
            }

            String subject = configured != null && StringUtils.hasText(configured.subject())
                    ? configured.subject()
                    : "Welcome!";
            String emailContent = configured != null && configured.bodyCustomised() && StringUtils.hasText(configured.htmlBody())
                    ? configured.htmlBody()
                    : templateEngine.process("welcome", context);
            sendEmailInternal(to, subject, emailContent);
            log.info("Welcome email sent to: {}", to);
        } catch (MessagingException e) {
            log.error("Failed to send welcome email to: {}", to, e);
            log.warn("Continuing despite welcome email failure");
        }
    }

    @Async
    @Override
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
    @Override
    public void sendSubscriptionRenewalEmail(String to, String organisationName, String planName,
            LocalDate renewalDate, BigDecimal amount) {
        try {
            String formattedDate = renewalDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            Context context = new Context();
            context.setVariable("organisationName", organisationName);
            context.setVariable("planName", planName);
            context.setVariable("renewalDate", formattedDate);
            context.setVariable("amount", amount);

            RenderedEmail configured = resolveTemplate(
                    "EMAIL_SUBSCRIPTION_RENEWAL",
                    Map.of(
                            "organisationName", organisationName != null ? organisationName : "",
                            "planName", planName != null ? planName : "",
                            "renewalDate", formattedDate,
                            "amount", amount != null ? amount : ""));
            if (configured != null && !configured.enabled()) {
                log.info("Skipping disabled email template EMAIL_SUBSCRIPTION_RENEWAL for recipient: {}", to);
                return;
            }

            String subject = configured != null && StringUtils.hasText(configured.subject())
                    ? configured.subject()
                    : "Subscription Renewal Notice";
            String emailContent = configured != null && configured.bodyCustomised() && StringUtils.hasText(configured.htmlBody())
                    ? configured.htmlBody()
                    : templateEngine.process("subscription-renewal", context);
            sendEmailInternal(to, subject, emailContent);
            log.info("Subscription renewal email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send subscription renewal email to: {}", to, e);
        }
    }

    @Async
    @Override
    public void sendTrialEndingEmail(String to, String organisationName, LocalDate trialEndDate) {
        try {
            String formattedDate = trialEndDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            Context context = new Context();
            context.setVariable("organisationName", organisationName);
            context.setVariable("trialEndDate", formattedDate);

            RenderedEmail configured = resolveTemplate(
                    "EMAIL_SUBSCRIPTION_TRIAL_ENDING",
                    Map.of(
                            "organisationName", organisationName != null ? organisationName : "",
                            "trialEndDate", formattedDate));
            if (configured != null && !configured.enabled()) {
                log.info("Skipping disabled email template EMAIL_SUBSCRIPTION_TRIAL_ENDING for recipient: {}", to);
                return;
            }

            String subject = configured != null && StringUtils.hasText(configured.subject())
                    ? configured.subject()
                    : "Your Trial is Ending Soon";
            String emailContent = configured != null && configured.bodyCustomised() && StringUtils.hasText(configured.htmlBody())
                    ? configured.htmlBody()
                    : templateEngine.process("subscription-trial-ending", context);
            sendEmailInternal(to, subject, emailContent);
            log.info("Trial ending email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send trial ending email to: {}", to, e);
        }
    }

    @Async
    @Override
    public void sendSubscriptionExpiredEmail(String to, String organisationName, LocalDate expiryDate) {
        try {
            String formattedDate = expiryDate.format(DateTimeFormatter.ofPattern("MMMM d, yyyy"));
            Context context = new Context();
            context.setVariable("organisationName", organisationName);
            context.setVariable("expiryDate", formattedDate);

            RenderedEmail configured = resolveTemplate(
                    "EMAIL_SUBSCRIPTION_EXPIRED",
                    Map.of(
                            "organisationName", organisationName != null ? organisationName : "",
                            "expiryDate", formattedDate));
            if (configured != null && !configured.enabled()) {
                log.info("Skipping disabled email template EMAIL_SUBSCRIPTION_EXPIRED for recipient: {}", to);
                return;
            }

            String subject = configured != null && StringUtils.hasText(configured.subject())
                    ? configured.subject()
                    : "Subscription Expired";
            String emailContent = configured != null && configured.bodyCustomised() && StringUtils.hasText(configured.htmlBody())
                    ? configured.htmlBody()
                    : templateEngine.process("subscription-expired", context);
            sendEmailInternal(to, subject, emailContent);
            log.info("Subscription expired email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send subscription expired email to: {}", to, e);
        }
    }

    private RenderedEmail resolveTemplate(String templateKey, Map<String, Object> variables) {
        if (messageTemplateConfigApi == null) {
            return null;
        }
        try {
            return messageTemplateConfigApi.renderEmail(templateKey, TenantContext.getCurrentTenantId(), variables);
        } catch (Exception e) {
            log.warn("Could not resolve configured email template {} — falling back to built-in template: {}",
                    templateKey, e.getMessage());
            return null;
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

    private void sendEmailInternal(String to, String subject, String content, String unsubscribeUrl)
            throws MessagingException {
        // Enforce deliverability check: skip send if recipient is actively suppressed
        if (suppressionService.isSuppressed(to)) {
            log.warn("Suppressed email recipient detected [{}]. Aborting outbound send for subject: {}", to, subject);
            return;
        }

        UUID organisationId = TenantContext.getCurrentTenantId();
        UUID gymId = TenantContext.getCurrentGymId();

        Optional<WhitelabelProfile> whitelabelOpt = whitelabelApi.findProfile(organisationId, gymId);

        JavaMailSender mailSender;
        String from;

        if (whitelabelOpt.isPresent() && whitelabelOpt.get().smtpEnabled()) {
            WhitelabelProfile settings = whitelabelOpt.get();
            mailSender = whitelabelApi.findTenantMailSender(organisationId, gymId).orElseThrow();

            from = StringUtils.hasText(settings.smtpFromEmail())
                    ? settings.smtpFromEmail()
                    : (StringUtils.hasText(settings.smtpUsername()) ? settings.smtpUsername() : defaultFromEmail);

            if (StringUtils.hasText(settings.smtpFromName())) {
                from = settings.smtpFromName() + " <" + from + ">";
            } else if (StringUtils.hasText(settings.brandName())) {
                from = settings.brandName() + " <" + from + ">";
            }
        } else {
            // No tenant custom SMTP (or none applicable, e.g. unauthenticated /api/auth/**
            // requests) —
            // fall back to the system default sender instead of failing the request.
            // BUG-001.
            mailSender = defaultMailSender;
            from = defaultFromEmail;
        }

        MimeMessage message = mailSender.createMimeMessage();
        MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

        helper.setFrom(from);
        helper.setTo(to);
        helper.setSubject(subject);
        helper.setText(content, true);
        if (content.contains("cid:" + LOGO_CONTENT_ID)) {
            helper.addInline(LOGO_CONTENT_ID, LOGO, "image/png"); // after setText, as Spring requires
        }

        // Attach AWS SES Tenant, Configuration Set, and tag headers when using system
        // sender
        if (mailSender == defaultMailSender) {
            String sesTenant = sesTenantResolver.resolveTenant(organisationId, gymId);
            if (StringUtils.hasText(sesTenant)) {
                message.setHeader("X-SES-TENANT", sesTenant);
            }

            String sesConfigSet = sesConfigurationSetResolver.resolveConfigurationSet(organisationId, gymId);
            if (StringUtils.hasText(sesConfigSet)) {
                message.setHeader("X-SES-CONFIGURATION-SET", sesConfigSet);
            }

            message.setHeader("X-SES-MESSAGE-TAGS",
                    "app=" + (StringUtils.hasText(sesTenant) ? sesTenant : "gymmatehub") + ",type=transactional");
        }

        // Attach RFC 8058 one-click unsubscribe headers if an unsubscribe link is
        // supplied
        if (StringUtils.hasText(unsubscribeUrl)) {
            message.setHeader("List-Unsubscribe", "<" + unsubscribeUrl + ">, <mailto:unsubscribe@gymmatehub.com>");
            message.setHeader("List-Unsubscribe-Post", "List-Unsubscribe=One-Click");
        }

        mailSender.send(message);
    }
}
