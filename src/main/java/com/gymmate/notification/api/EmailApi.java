package com.gymmate.notification.api;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Public facade for transactional e-mail. Delivery is asynchronous; the tenant's own SMTP
 * (whitelabel) is used when configured, the platform sender otherwise.
 */
public interface EmailApi {

    void sendPasswordResetEmail(String to, String name, String resetLink);

    /** Sends the verification OTP and publishes its delivery status to the user's status stream. */
    void sendOtpEmail(String to, String firstName, String otp, int validityMinutes, String userId);

    void sendWelcomeEmail(String to, String firstName);

    void sendHtmlEmail(String to, String subject, String htmlBody);

    void sendSubscriptionRenewalEmail(String to, String organisationName, String planName,
                                      LocalDate renewalDate, BigDecimal amount);

    void sendTrialEndingEmail(String to, String organisationName, LocalDate trialEndDate);

    void sendSubscriptionExpiredEmail(String to, String organisationName, LocalDate expiryDate);
}
