-- ============================================================================
-- V22: Message Template Configurations Table
-- Centralized platform-wide defaults (organisation_id IS NULL) and
-- organisation-level overrides for Email and Notification message templates.
-- ============================================================================

CREATE TABLE IF NOT EXISTS message_template_configs (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    version BIGINT NOT NULL DEFAULT 0,
    organisation_id UUID,

    template_key VARCHAR(100) NOT NULL,
    channel VARCHAR(30) NOT NULL DEFAULT 'EMAIL',
    name VARCHAR(150) NOT NULL,
    description VARCHAR(500),
    subject_or_title VARCHAR(300) NOT NULL,
    body_template TEXT,
    classpath_template VARCHAR(100),
    available_variables VARCHAR(500),
    body_customised BOOLEAN NOT NULL DEFAULT FALSE,

    -- BaseAuditEntity columns
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,

    CONSTRAINT fk_message_template_configs_org
        FOREIGN KEY (organisation_id) REFERENCES organisations(id) ON DELETE CASCADE
);

CREATE UNIQUE INDEX IF NOT EXISTS uq_message_template_configs_key_org
    ON message_template_configs (template_key, COALESCE(organisation_id, '00000000-0000-0000-0000-000000000000'::uuid));

CREATE INDEX IF NOT EXISTS idx_message_template_configs_org
    ON message_template_configs (organisation_id);

-- Seed platform-wide default templates (organisation_id IS NULL)
INSERT INTO message_template_configs (
    template_key, channel, name, description, subject_or_title,
    body_template, classpath_template, available_variables, body_customised, is_active
) VALUES
    ('EMAIL_PASSWORD_RESET', 'EMAIL', 'Password Reset Email',
     'Sent when a user requests a password reset link.',
     'Reset Your Password', '', 'password-reset', 'name,resetLink', FALSE, TRUE),

    ('EMAIL_REGISTRATION_OTP', 'EMAIL', 'Registration OTP Verification Email',
     'Sent with a one-time verification code during account registration.',
     'Your Verification Code', '', 'registration-otp', 'firstName,otp,validityMinutes', FALSE, TRUE),

    ('EMAIL_WELCOME', 'EMAIL', 'Welcome Email',
     'Sent after a user verifies their account registration.',
     'Welcome!', '', 'welcome', 'firstName', FALSE, TRUE),

    ('EMAIL_SUBSCRIPTION_RENEWAL', 'EMAIL', 'Subscription Renewal Notice Email',
     'Sent before an organisation subscription renews.',
     'Subscription Renewal Notice', '', 'subscription-renewal', 'organisationName,planName,renewalDate,amount', FALSE, TRUE),

    ('EMAIL_SUBSCRIPTION_TRIAL_ENDING', 'EMAIL', 'Subscription Trial Ending Email',
     'Sent 2 days before an organisation free trial expires.',
     'Your Trial is Ending Soon', '', 'subscription-trial-ending', 'organisationName,trialEndDate', FALSE, TRUE),

    ('EMAIL_SUBSCRIPTION_EXPIRED', 'EMAIL', 'Subscription Expired Email',
     'Sent when an organisation subscription expires.',
     'Subscription Expired', '', 'subscription-expired', 'organisationName,expiryDate', FALSE, TRUE),

    ('EMAIL_BILLING_TRIAL_ENDING_REMINDER', 'EMAIL', 'Billing Trial Ending Reminder (3 Days)',
     'Sent 3 days before a billing trial ends with plan price and management link.',
     'Your GymMateHub Trial Ends in 3 Days',
     '<p>Hi {{gymName}},</p><p>Your 14-day free trial will end on <strong>{{trialEndDate}}</strong>. You are currently on the <strong>{{tierName}}</strong> plan at <strong>${{price}}/month</strong>.</p><p><a href="{{frontendUrl}}/gym/settings/subscription">Manage Subscription</a></p>',
     NULL, 'gymName,tierName,price,trialEndDate,frontendUrl', TRUE, TRUE),

    ('EMAIL_BILLING_PAYMENT_SUCCESS', 'EMAIL', 'Billing Payment Received Email',
     'Sent when a subscription invoice payment succeeds.',
     'Payment Received - ${{amount}}',
     '<p>Hi {{gymName}},</p><p>Thank you for your payment of <strong>${{amount}}</strong> (Invoice: {{invoiceNumber}}, Next billing date: {{nextBillingDate}}).</p><p>{{invoiceLinksHtml}}</p>',
     NULL, 'gymName,amount,invoiceNumber,nextBillingDate,invoiceUrl,pdfUrl,invoiceLinksHtml', TRUE, TRUE),

    ('EMAIL_BILLING_PAYMENT_FAILED', 'EMAIL', 'Billing Payment Failed Email',
     'Sent when a subscription payment fails.',
     '⚠️ Payment Failed - Action Required',
     '<p>Hi {{gymName}},</p><p>We were unable to process your payment of <strong>${{amount}}</strong>. Reason: {{failureReason}}. Next retry: {{nextRetryDate}}.</p><p><a href="{{frontendUrl}}/gym/settings/payments">Update Payment Method</a></p>',
     NULL, 'gymName,amount,failureReason,nextRetryDate,frontendUrl', TRUE, TRUE),

    ('EMAIL_BILLING_SUBSCRIPTION_CANCELLED', 'EMAIL', 'Billing Subscription Cancelled Email',
     'Sent when a gym subscription is cancelled.',
     'Your GymMateHub Subscription Has Been Cancelled',
     '<p>Hi {{gymName}},</p><p>Your GymMateHub subscription has been cancelled. You will continue to have access until <strong>{{accessEndsAt}}</strong>.</p><p><a href="{{frontendUrl}}/gym/settings/subscription">Reactivate Subscription</a></p>',
     NULL, 'gymName,accessEndsAt,frontendUrl', TRUE, TRUE),

    ('EMAIL_BILLING_SUBSCRIPTION_REACTIVATED', 'EMAIL', 'Billing Subscription Reactivated Email',
     'Sent when a cancelled subscription is reactivated.',
     'Welcome Back! Your GymMateHub Subscription is Active',
     '<p>Hi {{gymName}},</p><p>Great news! Your GymMateHub subscription has been reactivated on the <strong>{{tierName}}</strong> plan.</p><p><a href="{{frontendUrl}}/gym/dashboard">Go to Dashboard</a></p>',
     NULL, 'gymName,tierName,frontendUrl', TRUE, TRUE),

    ('EMAIL_BILLING_TRIAL_STARTED', 'EMAIL', 'Billing Free Trial Started Email',
     'Sent when a gym starts its 14-day free trial.',
     'Welcome to GymMateHub! Your Free Trial Has Started',
     '<p>Hi {{gymName}},</p><p>Your 14-day free trial of the <strong>{{tierName}}</strong> plan has started and ends on <strong>{{trialEndDate}}</strong>.</p><p><a href="{{frontendUrl}}/gym/dashboard">Start Exploring</a></p>',
     NULL, 'gymName,tierName,trialEndDate,frontendUrl', TRUE, TRUE),

    ('EMAIL_BILLING_UPGRADE_SUGGESTED', 'EMAIL', 'Billing Plan Upgrade Suggested Email',
     'Sent when member usage overage exceeds 50% of the base plan cost.',
     'Consider Upgrading Your GymMateHub Plan',
     '<p>Hi {{organisationName}},</p><p>Your current member count ({{memberCount}}) has exceeded your plan limits. Overage charges now exceed 50% of your base cost.</p><p>We recommend upgrading to reduce costs and unlock more features.</p><p><a href="{{frontendUrl}}/gym/settings/subscription">View Plans</a></p>',
     NULL, 'organisationName,memberCount,memberLimit,currentTier,frontendUrl', TRUE, TRUE),

    ('EMAIL_USER_INVITE', 'EMAIL', 'Team Member Invitation Email',
     'Sent when a gym owner or manager invites a staff member, manager, or trainer.',
     'You''ve been invited to join {{gymName}}',
     'Hello {{firstName}},<br><br>{{inviterName}} has invited you to join {{gymName}} as a {{role}}.<br><br>Click here to accept: <a href="{{inviteLink}}">{{inviteLink}}</a>',
     NULL, 'firstName,inviterName,gymName,role,inviteLink', TRUE, TRUE),

    ('SUBSCRIPTION_UPGRADE_SUGGESTED', 'NOTIFICATION', 'Subscription Upgrade Suggested Notification',
     'In-app notification when member overage charges exceed 50% of base plan cost.',
     '📈 Consider Upgrading Your Plan',
     'Your current usage has exceeded your plan limits. Overage charges now exceed 50% of your base plan cost. Upgrading to a higher tier could save you money.',
     NULL, 'currentTier,memberCount,memberLimit', TRUE, TRUE),

    ('SUBSCRIPTION_EXPIRED', 'NOTIFICATION', 'Subscription Expired Notification',
     'In-app notification when an organisation subscription expires.',
     'Subscription Expired',
     'Your subscription has expired. Please renew to continue using GymMateHub.',
     NULL, 'subscriptionId', TRUE, TRUE),

    ('SUBSCRIPTION_SUSPENDED', 'NOTIFICATION', 'Subscription Suspended Notification',
     'In-app notification when a past-due subscription exceeds the grace period.',
     'Subscription Suspended',
     'Your subscription has been suspended after a payment failure was not resolved within the grace period. Please update your payment method to restore access.',
     NULL, 'subscriptionId', TRUE, TRUE),

    ('SUBSCRIPTION_RENEWAL', 'NOTIFICATION', 'Subscription Renewal Notification',
     'In-app notification 3 days before a subscription renews.',
     'Subscription Renewal',
     'Your subscription will renew in 3 days.',
     NULL, 'subscriptionId,renewalDate', TRUE, TRUE),

    ('TRIAL_ENDING', 'NOTIFICATION', 'Trial Ending Soon Notification',
     'In-app notification 2 days before a free trial ends.',
     'Trial Ending Soon',
     'Your free trial is ending in 2 days. Upgrade now to keep using GymMateHub.',
     NULL, 'subscriptionId,trialEnd', TRUE, TRUE),

    ('STRIPE_CONNECT_DEAUTHORIZED', 'NOTIFICATION', 'Stripe Account Disconnected Notification',
     'In-app notification when a gym''s Stripe Connect account is deauthorized.',
     '⚠️ Stripe Account Disconnected',
     'Your Stripe Connect account has been disconnected. Member payments will not be processed until you reconnect.',
     NULL, 'gymId', TRUE, TRUE),

    ('PAYMENT_FAILED', 'NOTIFICATION', 'Payment Failed Notification',
     'In-app notification when a subscription or member payment fails.',
     '⚠️ Payment Failed',
     'Payment of ${{amount}} failed: {{failureReason}}. Next retry: {{nextRetryDate}}',
     NULL, 'gymId,amount,failureReason,nextRetryDate,invoiceId', TRUE, TRUE),

    ('PAYMENT_SUCCESS', 'NOTIFICATION', 'Payment Received Notification',
     'In-app notification when a subscription payment succeeds.',
     '✅ Payment Received',
     'Payment of ${{amount}} received for invoice {{invoiceNumber}}.',
     NULL, 'gymId,amount,invoiceNumber,invoiceUrl,periodEnd', TRUE, TRUE),

    ('SUBSCRIPTION_EXPIRING', 'NOTIFICATION', 'Subscription Expiring Notification',
     'In-app notification when a subscription is approaching expiration.',
     '⏳ Subscription Expiring Soon',
     'Your {{tierName}} subscription (${{price}}/month) expires in {{daysUntilExpiry}} days.',
     NULL, 'subscriptionId,tierName,price,expiresAt,daysUntilExpiry', TRUE, TRUE),

    ('MEMBER_JOINED', 'NOTIFICATION', 'New Member Joined Notification',
     'In-app notification when a new member joins a gym.',
     '👋 New Member Joined',
     '{{memberName}} joined on {{membershipPlan}} plan',
     NULL, 'gymId,memberId,memberName,memberEmail,membershipPlan', TRUE, TRUE)
ON CONFLICT DO NOTHING;

