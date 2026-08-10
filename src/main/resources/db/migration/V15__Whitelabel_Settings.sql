-- ============================================================================
-- V15: Whitelabel Settings Table
-- Allows Organisations and Gyms to configure custom branding, SMTP,
-- WhatsApp numbers/credentials, and Newsletter provider API keys.
-- ============================================================================

CREATE TABLE IF NOT EXISTS whitelabel_settings (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL,
    gym_id UUID,

    -- Branding & Customization
    brand_name VARCHAR(100),
    logo_url VARCHAR(500),
    favicon_url VARCHAR(500),
    primary_color VARCHAR(20),
    secondary_color VARCHAR(20),
    custom_domain VARCHAR(255),
    email_header_logo_url VARCHAR(500),
    email_footer_text TEXT,
    support_email VARCHAR(255),
    support_phone VARCHAR(50),

    -- Custom SMTP Configuration
    smtp_enabled BOOLEAN DEFAULT FALSE,
    smtp_host VARCHAR(255),
    smtp_port INTEGER,
    smtp_username VARCHAR(255),
    smtp_password_encrypted TEXT,
    smtp_security VARCHAR(20) DEFAULT 'STARTTLS',
    smtp_from_email VARCHAR(255),
    smtp_from_name VARCHAR(255),

    -- WhatsApp Business API Credentials
    whatsapp_enabled BOOLEAN DEFAULT FALSE,
    whatsapp_provider VARCHAR(50) DEFAULT 'META_CLOUD_API',
    whatsapp_phone_number VARCHAR(50),
    whatsapp_phone_number_id VARCHAR(100),
    whatsapp_business_id VARCHAR(100),
    whatsapp_api_key_encrypted TEXT,

    -- Newsletter Provider Settings & API Keys
    newsletter_enabled BOOLEAN DEFAULT FALSE,
    newsletter_provider VARCHAR(50) DEFAULT 'CUSTOM_SMTP',
    newsletter_api_key_encrypted TEXT,
    newsletter_list_id VARCHAR(100),
    newsletter_sender_email VARCHAR(255),
    newsletter_sender_name VARCHAR(255),

    -- BaseAuditEntity columns
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE,

    -- Foreign keys & unique constraint
    CONSTRAINT fk_whitelabel_settings_org FOREIGN KEY (organisation_id) REFERENCES organisations(id) ON DELETE CASCADE,
    CONSTRAINT fk_whitelabel_settings_gym FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE,
    CONSTRAINT uq_whitelabel_org_gym UNIQUE (organisation_id, gym_id)
);

CREATE INDEX IF NOT EXISTS idx_whitelabel_settings_org ON whitelabel_settings(organisation_id);
CREATE INDEX IF NOT EXISTS idx_whitelabel_settings_gym ON whitelabel_settings(gym_id);
