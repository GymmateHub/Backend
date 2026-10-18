-- Migration: V16__Email_Suppression_And_Ses_Events.sql
-- Description: Creates email suppression and SNS processed message tables for AWS SES deliverability & feedback loop.

CREATE TABLE IF NOT EXISTS email_suppressions (
    id UUID PRIMARY KEY,
    email VARCHAR(255) NOT NULL,
    reason VARCHAR(50) NOT NULL,
    bounce_type VARCHAR(50),
    bounce_sub_type VARCHAR(100),
    diagnostic_code TEXT,
    transient_bounce_count INT NOT NULL DEFAULT 1,
    organisation_id UUID,
    gym_id UUID,
    is_active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    created_by VARCHAR(255),
    updated_by VARCHAR(255)
);

CREATE INDEX IF NOT EXISTS idx_email_suppressions_email ON email_suppressions(email);
CREATE INDEX IF NOT EXISTS idx_email_suppressions_active ON email_suppressions(is_active);
CREATE INDEX IF NOT EXISTS idx_email_suppressions_lookup ON email_suppressions(email, is_active);
CREATE INDEX IF NOT EXISTS idx_email_suppressions_org_gym ON email_suppressions(organisation_id, gym_id);

CREATE TABLE IF NOT EXISTS sns_processed_messages (
    id UUID PRIMARY KEY,
    message_id VARCHAR(255) NOT NULL UNIQUE,
    topic_arn VARCHAR(500),
    message_type VARCHAR(100),
    event_type VARCHAR(100),
    processed_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_sns_processed_message_id ON sns_processed_messages(message_id);
