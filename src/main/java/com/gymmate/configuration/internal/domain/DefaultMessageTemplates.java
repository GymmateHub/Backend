package com.gymmate.configuration.internal.domain;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Built-in catalog of all platform default email and notification message templates,
 * plus pure-Java placeholder interpolation ({{var}} and ${var}).
 *
 * <p>Acts as the canonical source of truth when resetting a template to its factory default
 * or when a template key has not yet been seeded in the database.
 */
public final class DefaultMessageTemplates {

    public static final String EMAIL_PASSWORD_RESET = "EMAIL_PASSWORD_RESET";
    public static final String EMAIL_REGISTRATION_OTP = "EMAIL_REGISTRATION_OTP";
    public static final String EMAIL_WELCOME = "EMAIL_WELCOME";
    public static final String EMAIL_SUBSCRIPTION_RENEWAL = "EMAIL_SUBSCRIPTION_RENEWAL";
    public static final String EMAIL_SUBSCRIPTION_TRIAL_ENDING = "EMAIL_SUBSCRIPTION_TRIAL_ENDING";
    public static final String EMAIL_SUBSCRIPTION_EXPIRED = "EMAIL_SUBSCRIPTION_EXPIRED";
    public static final String EMAIL_BILLING_TRIAL_ENDING_REMINDER = "EMAIL_BILLING_TRIAL_ENDING_REMINDER";
    public static final String EMAIL_BILLING_PAYMENT_SUCCESS = "EMAIL_BILLING_PAYMENT_SUCCESS";
    public static final String EMAIL_BILLING_PAYMENT_FAILED = "EMAIL_BILLING_PAYMENT_FAILED";
    public static final String EMAIL_BILLING_SUBSCRIPTION_CANCELLED = "EMAIL_BILLING_SUBSCRIPTION_CANCELLED";
    public static final String EMAIL_BILLING_SUBSCRIPTION_REACTIVATED = "EMAIL_BILLING_SUBSCRIPTION_REACTIVATED";
    public static final String EMAIL_BILLING_TRIAL_STARTED = "EMAIL_BILLING_TRIAL_STARTED";
    public static final String EMAIL_BILLING_UPGRADE_SUGGESTED = "EMAIL_BILLING_UPGRADE_SUGGESTED";
    public static final String EMAIL_USER_INVITE = "EMAIL_USER_INVITE";

    public static final String NOTIFICATION_SUBSCRIPTION_UPGRADE_SUGGESTED = "SUBSCRIPTION_UPGRADE_SUGGESTED";
    public static final String NOTIFICATION_SUBSCRIPTION_EXPIRED = "SUBSCRIPTION_EXPIRED";
    public static final String NOTIFICATION_SUBSCRIPTION_SUSPENDED = "SUBSCRIPTION_SUSPENDED";
    public static final String NOTIFICATION_SUBSCRIPTION_RENEWAL = "SUBSCRIPTION_RENEWAL";
    public static final String NOTIFICATION_TRIAL_ENDING = "TRIAL_ENDING";
    public static final String NOTIFICATION_STRIPE_CONNECT_DEAUTHORIZED = "STRIPE_CONNECT_DEAUTHORIZED";
    public static final String NOTIFICATION_PAYMENT_FAILED = "PAYMENT_FAILED";
    public static final String NOTIFICATION_PAYMENT_SUCCESS = "PAYMENT_SUCCESS";
    public static final String NOTIFICATION_SUBSCRIPTION_EXPIRING = "SUBSCRIPTION_EXPIRING";
    public static final String NOTIFICATION_MEMBER_JOINED = "MEMBER_JOINED";
    public static final String NOTIFICATION_CHARGE_DISPUTED = "CHARGE_DISPUTED";
    public static final String NOTIFICATION_CHARGE_REFUNDED = "CHARGE_REFUNDED";
    public static final String NOTIFICATION_MEMBERSHIP_EXPIRED = "MEMBERSHIP_EXPIRED";
    public static final String NOTIFICATION_SUBSCRIPTION_PAUSED = "SUBSCRIPTION_PAUSED";
    public static final String NOTIFICATION_WAITLIST_PROMOTED = "WAITLIST_PROMOTED";

    private static final Pattern PLACEHOLDER_PATTERN =
            Pattern.compile("\\{\\{\\s*([a-zA-Z0-9_.-]+)\\s*\\}\\}|\\$\\{\\s*([a-zA-Z0-9_.-]+)\\s*\\}");

    private static final Map<String, MessageTemplateConfig> DEFAULTS;

    static {
        Map<String, MessageTemplateConfig> map = new LinkedHashMap<>();

        // 1. Classpath-backed transactional emails (EmailService)
        register(map, MessageTemplateConfig.builder()
                .templateKey(EMAIL_PASSWORD_RESET)
                .channel(MessageTemplateChannel.EMAIL)
                .name("Password Reset Email")
                .description("Sent when a user requests a password reset link.")
                .subjectOrTitle("Reset Your Password")
                .bodyTemplate("")
                .classpathTemplate("password-reset")
                .availableVariables("name,resetLink")
                .bodyCustomised(false)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(EMAIL_REGISTRATION_OTP)
                .channel(MessageTemplateChannel.EMAIL)
                .name("Registration OTP Verification Email")
                .description("Sent with a one-time verification code during account registration.")
                .subjectOrTitle("Your Verification Code")
                .bodyTemplate("")
                .classpathTemplate("registration-otp")
                .availableVariables("firstName,otp,validityMinutes")
                .bodyCustomised(false)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(EMAIL_WELCOME)
                .channel(MessageTemplateChannel.EMAIL)
                .name("Welcome Email")
                .description("Sent after a user verifies their account registration.")
                .subjectOrTitle("Welcome!")
                .bodyTemplate("")
                .classpathTemplate("welcome")
                .availableVariables("firstName")
                .bodyCustomised(false)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(EMAIL_SUBSCRIPTION_RENEWAL)
                .channel(MessageTemplateChannel.EMAIL)
                .name("Subscription Renewal Notice Email")
                .description("Sent before an organisation subscription renews.")
                .subjectOrTitle("Subscription Renewal Notice")
                .bodyTemplate("")
                .classpathTemplate("subscription-renewal")
                .availableVariables("organisationName,planName,renewalDate,amount")
                .bodyCustomised(false)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(EMAIL_SUBSCRIPTION_TRIAL_ENDING)
                .channel(MessageTemplateChannel.EMAIL)
                .name("Subscription Trial Ending Email")
                .description("Sent 2 days before an organisation free trial expires.")
                .subjectOrTitle("Your Trial is Ending Soon")
                .bodyTemplate("")
                .classpathTemplate("subscription-trial-ending")
                .availableVariables("organisationName,trialEndDate")
                .bodyCustomised(false)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(EMAIL_SUBSCRIPTION_EXPIRED)
                .channel(MessageTemplateChannel.EMAIL)
                .name("Subscription Expired Email")
                .description("Sent when an organisation subscription expires.")
                .subjectOrTitle("Subscription Expired")
                .bodyTemplate("")
                .classpathTemplate("subscription-expired")
                .availableVariables("organisationName,expiryDate")
                .bodyCustomised(false)
                .build());

        // 2. Billing & Invite emails (PaymentNotificationService, SubscriptionService, InviteService)
        register(map, MessageTemplateConfig.builder()
                .templateKey(EMAIL_BILLING_TRIAL_ENDING_REMINDER)
                .channel(MessageTemplateChannel.EMAIL)
                .name("Billing Trial Ending Reminder (3 Days)")
                .description("Sent 3 days before a billing trial ends with plan price and management link.")
                .subjectOrTitle("Your GymMateHub Trial Ends in 3 Days")
                .bodyTemplate("""
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background: #465FFF; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                                .content { background: #fff; padding: 30px; border: 1px solid #e0e0e0; border-radius: 0 0 8px 8px; }
                                .cta-button { display: inline-block; background: #465FFF; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                                .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>Your Trial Ends Soon!</h1>
                                </div>
                                <div class="content">
                                    <p>Hi {{gymName}},</p>
                                    <p>Your 14-day free trial will end on <strong>{{trialEndDate}}</strong>.</p>
                                    <p>You're currently on the <strong>{{tierName}}</strong> plan at <strong>${{price}}/month</strong>.</p>
                                    <p>To continue enjoying GymMateHub without interruption, no action is needed. Your card on file will be charged automatically.</p>
                                    <p>Want to make changes? You can:</p>
                                    <ul>
                                        <li>Update your payment method</li>
                                        <li>Change your subscription plan</li>
                                        <li>Cancel before the trial ends</li>
                                    </ul>
                                    <a href="{{frontendUrl}}/gym/settings/subscription" class="cta-button">Manage Subscription</a>
                                    <p>Questions? Reply to this email or contact our support team.</p>
                                    <p>Thanks for trying GymMateHub!</p>
                                </div>
                                <div class="footer">
                                    <p>© 2026 GymMateHub. All rights reserved.</p>
                                </div>
                            </div>
                        </body>
                        </html>""")
                .availableVariables("gymName,tierName,price,trialEndDate,frontendUrl")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(EMAIL_BILLING_PAYMENT_SUCCESS)
                .channel(MessageTemplateChannel.EMAIL)
                .name("Billing Payment Received Email")
                .description("Sent when a subscription invoice payment succeeds.")
                .subjectOrTitle("Payment Received - ${{amount}}")
                .bodyTemplate("""
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
                                    <h1>Payment Received</h1>
                                </div>
                                <div class="content">
                                    <p>Hi {{gymName}},</p>
                                    <p>Thank you for your payment!</p>
                                    <p class="amount">${{amount}}</p>
                                    <p><strong>Invoice:</strong> {{invoiceNumber}}</p>
                                    <p><strong>Next billing date:</strong> {{nextBillingDate}}</p>
                                    <p>{{invoiceLinksHtml}}</p>
                                    <p>Thank you for being a GymMateHub customer!</p>
                                </div>
                                <div class="footer">
                                    <p>© 2026 GymMateHub. All rights reserved.</p>
                                </div>
                            </div>
                        </body>
                        </html>""")
                .availableVariables("gymName,amount,invoiceNumber,nextBillingDate,invoiceUrl,pdfUrl,invoiceLinksHtml")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(EMAIL_BILLING_PAYMENT_FAILED)
                .channel(MessageTemplateChannel.EMAIL)
                .name("Billing Payment Failed Email")
                .description("Sent when a subscription payment fails.")
                .subjectOrTitle("⚠️ Payment Failed - Action Required")
                .bodyTemplate("""
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
                                    <h1>Payment Failed</h1>
                                </div>
                                <div class="content">
                                    <p>Hi {{gymName}},</p>
                                    <p>We were unable to process your payment of <strong>${{amount}}</strong>.</p>
                                    <div class="warning">
                                        <strong>Reason:</strong> {{failureReason}}
                                    </div>
                                    <p>We will automatically retry on <strong>{{nextRetryDate}}</strong>.</p>
                                    <p>To avoid service interruption, please update your payment method:</p>
                                    <a href="{{frontendUrl}}/gym/settings/payments" class="cta-button">Update Payment Method</a>
                                    <p>Need help? Contact our support team.</p>
                                </div>
                                <div class="footer">
                                    <p>© 2026 GymMateHub. All rights reserved.</p>
                                </div>
                            </div>
                        </body>
                        </html>""")
                .availableVariables("gymName,amount,failureReason,nextRetryDate,frontendUrl")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(EMAIL_BILLING_SUBSCRIPTION_CANCELLED)
                .channel(MessageTemplateChannel.EMAIL)
                .name("Billing Subscription Cancelled Email")
                .description("Sent when a gym subscription is cancelled.")
                .subjectOrTitle("Your GymMateHub Subscription Has Been Cancelled")
                .bodyTemplate("""
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background: #6c757d; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                                .content { background: #fff; padding: 30px; border: 1px solid #e0e0e0; border-radius: 0 0 8px 8px; }
                                .cta-button { display: inline-block; background: #465FFF; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                                .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>Subscription Cancelled</h1>
                                </div>
                                <div class="content">
                                    <p>Hi {{gymName}},</p>
                                    <p>Your GymMateHub subscription has been cancelled.</p>
                                    <p>You'll continue to have access until <strong>{{accessEndsAt}}</strong>.</p>
                                    <p>We're sorry to see you go! If you change your mind, you can reactivate your subscription anytime:</p>
                                    <a href="{{frontendUrl}}/gym/settings/subscription" class="cta-button">Reactivate Subscription</a>
                                    <p>We'd love to hear your feedback on how we can improve.</p>
                                </div>
                                <div class="footer">
                                    <p>© 2026 GymMateHub. All rights reserved.</p>
                                </div>
                            </div>
                        </body>
                        </html>""")
                .availableVariables("gymName,accessEndsAt,frontendUrl")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(EMAIL_BILLING_SUBSCRIPTION_REACTIVATED)
                .channel(MessageTemplateChannel.EMAIL)
                .name("Billing Subscription Reactivated Email")
                .description("Sent when a cancelled subscription is reactivated.")
                .subjectOrTitle("Welcome Back! Your GymMateHub Subscription is Active")
                .bodyTemplate("""
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background: #465FFF; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                                .content { background: #fff; padding: 30px; border: 1px solid #e0e0e0; border-radius: 0 0 8px 8px; }
                                .cta-button { display: inline-block; background: #465FFF; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                                .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>Welcome Back!</h1>
                                </div>
                                <div class="content">
                                    <p>Hi {{gymName}},</p>
                                    <p>Great news! Your GymMateHub subscription has been reactivated.</p>
                                    <p>You're now on the <strong>{{tierName}}</strong> plan with full access to all features.</p>
                                    <a href="{{frontendUrl}}/gym/dashboard" class="cta-button">Go to Dashboard</a>
                                    <p>Thank you for choosing GymMateHub!</p>
                                </div>
                                <div class="footer">
                                    <p>© 2026 GymMateHub. All rights reserved.</p>
                                </div>
                            </div>
                        </body>
                        </html>""")
                .availableVariables("gymName,tierName,frontendUrl")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(EMAIL_BILLING_TRIAL_STARTED)
                .channel(MessageTemplateChannel.EMAIL)
                .name("Billing Free Trial Started Email")
                .description("Sent when a gym starts its 14-day free trial.")
                .subjectOrTitle("Welcome to GymMateHub! Your Free Trial Has Started")
                .bodyTemplate("""
                        <!DOCTYPE html>
                        <html>
                        <head>
                            <style>
                                body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                                .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                                .header { background: #465FFF; color: white; padding: 30px; text-align: center; border-radius: 8px 8px 0 0; }
                                .content { background: #fff; padding: 30px; border: 1px solid #e0e0e0; border-radius: 0 0 8px 8px; }
                                .cta-button { display: inline-block; background: #465FFF; color: white; padding: 12px 30px; text-decoration: none; border-radius: 5px; margin: 20px 0; }
                                .feature-list { background: #f8f9fa; padding: 20px; border-radius: 5px; margin: 20px 0; }
                                .footer { text-align: center; color: #888; font-size: 12px; margin-top: 20px; }
                            </style>
                        </head>
                        <body>
                            <div class="container">
                                <div class="header">
                                    <h1>Welcome to GymMateHub!</h1>
                                </div>
                                <div class="content">
                                    <p>Hi {{gymName}},</p>
                                    <p>Your 14-day free trial of the <strong>{{tierName}}</strong> plan has started!</p>
                                    <p>Trial ends on: <strong>{{trialEndDate}}</strong></p>
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
                                    <a href="{{frontendUrl}}/gym/dashboard" class="cta-button">Start Exploring</a>
                                    <p>Need help getting started? Check out our <a href="{{frontendUrl}}/help">Help Center</a>.</p>
                                </div>
                                <div class="footer">
                                    <p>© 2026 GymMateHub. All rights reserved.</p>
                                </div>
                            </div>
                        </body>
                        </html>""")
                .availableVariables("gymName,tierName,trialEndDate,frontendUrl")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(EMAIL_BILLING_UPGRADE_SUGGESTED)
                .channel(MessageTemplateChannel.EMAIL)
                .name("Billing Plan Upgrade Suggested Email")
                .description("Sent when member usage overage exceeds 50% of the base plan cost.")
                .subjectOrTitle("Consider Upgrading Your GymMateHub Plan")
                .bodyTemplate("<p>Hi {{organisationName}},</p>"
                        + "<p>Your current member count ({{memberCount}}) has exceeded your plan limits. "
                        + "Overage charges now exceed 50% of your base cost.</p>"
                        + "<p>We recommend upgrading to reduce costs and unlock more features.</p>"
                        + "<p><a href='{{frontendUrl}}/gym/settings/subscription'>View Plans</a></p>")
                .availableVariables("organisationName,memberCount,memberLimit,currentTier,frontendUrl")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(EMAIL_USER_INVITE)
                .channel(MessageTemplateChannel.EMAIL)
                .name("Team Member Invitation Email")
                .description("Sent when a gym owner or manager invites a staff member, manager, or trainer.")
                .subjectOrTitle("You've been invited to join {{gymName}}")
                .bodyTemplate("Hello {{firstName}},<br><br>"
                        + "{{inviterName}} has invited you to join {{gymName}} as a {{role}}.<br><br>"
                        + "Click here to accept: <a href=\"{{inviteLink}}\">{{inviteLink}}</a>")
                .availableVariables("firstName,inviterName,gymName,role,inviteLink")
                .bodyCustomised(true)
                .build());

        // 3. System / In-App Notification Message Templates
        register(map, MessageTemplateConfig.builder()
                .templateKey(NOTIFICATION_SUBSCRIPTION_UPGRADE_SUGGESTED)
                .channel(MessageTemplateChannel.NOTIFICATION)
                .name("Subscription Upgrade Suggested Notification")
                .description("In-app notification when member overage charges exceed 50% of base plan cost.")
                .subjectOrTitle("📈 Consider Upgrading Your Plan")
                .bodyTemplate("Your current usage has exceeded your plan limits. Overage charges now exceed 50% of your base plan cost. Upgrading to a higher tier could save you money.")
                .availableVariables("currentTier,memberCount,memberLimit")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(NOTIFICATION_SUBSCRIPTION_EXPIRED)
                .channel(MessageTemplateChannel.NOTIFICATION)
                .name("Subscription Expired Notification")
                .description("In-app notification when an organisation subscription expires.")
                .subjectOrTitle("Subscription Expired")
                .bodyTemplate("Your subscription has expired. Please renew to continue using GymMateHub.")
                .availableVariables("subscriptionId")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(NOTIFICATION_SUBSCRIPTION_SUSPENDED)
                .channel(MessageTemplateChannel.NOTIFICATION)
                .name("Subscription Suspended Notification")
                .description("In-app notification when a past-due subscription exceeds the grace period.")
                .subjectOrTitle("Subscription Suspended")
                .bodyTemplate("Your subscription has been suspended after a payment failure was not resolved within the grace period. Please update your payment method to restore access.")
                .availableVariables("subscriptionId")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(NOTIFICATION_SUBSCRIPTION_RENEWAL)
                .channel(MessageTemplateChannel.NOTIFICATION)
                .name("Subscription Renewal Notification")
                .description("In-app notification 3 days before a subscription renews.")
                .subjectOrTitle("Subscription Renewal")
                .bodyTemplate("Your subscription will renew in 3 days.")
                .availableVariables("subscriptionId,renewalDate")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(NOTIFICATION_TRIAL_ENDING)
                .channel(MessageTemplateChannel.NOTIFICATION)
                .name("Trial Ending Soon Notification")
                .description("In-app notification 2 days before a free trial ends.")
                .subjectOrTitle("Trial Ending Soon")
                .bodyTemplate("Your free trial is ending in 2 days. Upgrade now to keep using GymMateHub.")
                .availableVariables("subscriptionId,trialEnd")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(NOTIFICATION_STRIPE_CONNECT_DEAUTHORIZED)
                .channel(MessageTemplateChannel.NOTIFICATION)
                .name("Stripe Account Disconnected Notification")
                .description("In-app notification when a gym's Stripe Connect account is deauthorized.")
                .subjectOrTitle("⚠️ Stripe Account Disconnected")
                .bodyTemplate("Your Stripe Connect account has been disconnected. Member payments will not be processed until you reconnect.")
                .availableVariables("gymId")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(NOTIFICATION_PAYMENT_FAILED)
                .channel(MessageTemplateChannel.NOTIFICATION)
                .name("Payment Failed Notification")
                .description("In-app notification when a subscription or member payment fails.")
                .subjectOrTitle("⚠️ Payment Failed")
                .bodyTemplate("Payment of ${{amount}} failed: {{failureReason}}. Next retry: {{nextRetryDate}}")
                .availableVariables("gymId,amount,failureReason,nextRetryDate,invoiceId")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(NOTIFICATION_PAYMENT_SUCCESS)
                .channel(MessageTemplateChannel.NOTIFICATION)
                .name("Payment Received Notification")
                .description("In-app notification when a subscription payment succeeds.")
                .subjectOrTitle("✅ Payment Received")
                .bodyTemplate("Payment of ${{amount}} received for invoice {{invoiceNumber}}.")
                .availableVariables("gymId,amount,invoiceNumber,invoiceUrl,periodEnd")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(NOTIFICATION_SUBSCRIPTION_EXPIRING)
                .channel(MessageTemplateChannel.NOTIFICATION)
                .name("Subscription Expiring Notification")
                .description("In-app notification when a subscription is approaching expiration.")
                .subjectOrTitle("⏳ Subscription Expiring Soon")
                .bodyTemplate("Your {{tierName}} subscription (${{price}}/month) expires in {{daysUntilExpiry}} days.")
                .availableVariables("subscriptionId,tierName,price,expiresAt,daysUntilExpiry")
                .bodyCustomised(true)
                .build());

        register(map, MessageTemplateConfig.builder()
                .templateKey(NOTIFICATION_MEMBER_JOINED)
                .channel(MessageTemplateChannel.NOTIFICATION)
                .name("New Member Joined Notification")
                .description("In-app notification when a new member joins a gym.")
                .subjectOrTitle("👋 New Member Joined")
                .bodyTemplate("{{memberName}} joined on {{membershipPlan}} plan")
                .availableVariables("gymId,memberId,memberName,memberEmail,membershipPlan")
                .bodyCustomised(true)
                .build());

        DEFAULTS = Collections.unmodifiableMap(map);
    }

    private DefaultMessageTemplates() {
    }

    private static void register(Map<String, MessageTemplateConfig> map, MessageTemplateConfig template) {
        map.put(template.getTemplateKey(), template);
    }

    public static List<MessageTemplateConfig> all() {
        return DEFAULTS.values().stream().map(DefaultMessageTemplates::copyOf).toList();
    }

    public static Optional<MessageTemplateConfig> findByKey(String templateKey) {
        if (templateKey == null) {
            return Optional.empty();
        }
        MessageTemplateConfig def = DEFAULTS.get(templateKey);
        return def == null ? Optional.empty() : Optional.of(copyOf(def));
    }

    public static MessageTemplateConfig copyOf(MessageTemplateConfig source) {
        return MessageTemplateConfig.builder()
                .organisationId(source.getOrganisationId())
                .templateKey(source.getTemplateKey())
                .channel(source.getChannel())
                .name(source.getName())
                .description(source.getDescription())
                .subjectOrTitle(source.getSubjectOrTitle())
                .bodyTemplate(source.getBodyTemplate())
                .classpathTemplate(source.getClasspathTemplate())
                .availableVariables(source.getAvailableVariables())
                .bodyCustomised(source.isBodyCustomised())
                .build();
    }

    /**
     * Interpolates {@code {{variable}}} and {@code ${variable}} placeholders in {@code template}
     * using values from {@code variables}. Missing variables resolve to an empty string.
     */
    public static String interpolate(String template, Map<String, Object> variables) {
        if (template == null || template.isEmpty()) {
            return "";
        }
        if (variables == null || variables.isEmpty()) {
            return template;
        }
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);
        StringBuilder sb = new StringBuilder(template.length());
        while (matcher.find()) {
            String key = matcher.group(1) != null ? matcher.group(1) : matcher.group(2);
            Object val = variables.get(key);
            String replacement = val != null ? String.valueOf(val) : "";
            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);
        return sb.toString();
    }
}

