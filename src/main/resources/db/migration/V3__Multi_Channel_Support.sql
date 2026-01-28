-- ============================================================================
-- V3: Drop and Recreate Newsletter Tables with Correct Schema
-- ============================================================================
-- Drops newsletter tables from V2 (which had missing columns) and recreates
-- them with all required BaseAuditEntity columns.
-- Generated: 2026-01-28
-- ============================================================================

-- ============================================================================
-- SECTION 1: DROP EXISTING TABLES (reverse order due to foreign keys)
-- ============================================================================

DROP TABLE IF EXISTS campaign_recipients CASCADE;
DROP TABLE IF EXISTS newsletter_campaigns CASCADE;
DROP TABLE IF EXISTS newsletter_templates CASCADE;

-- ============================================================================
-- SECTION 2: RECREATE NEWSLETTER TEMPLATES
-- Inherits from GymScopedEntity -> TenantEntity -> BaseAuditEntity
-- ============================================================================

CREATE TABLE newsletter_templates (
    -- BaseEntity
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    
    -- TenantEntity columns
    organisation_id UUID NOT NULL,
    
    -- GymScopedEntity columns
    gym_id UUID,
    
    -- NewsletterTemplate specific columns
    name VARCHAR(100) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    template_type VARCHAR(20) DEFAULT 'EMAIL',
    placeholders JSONB DEFAULT '[]',

    -- BaseAuditEntity columns
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE
    
);

CREATE INDEX idx_newsletter_templates_org ON newsletter_templates(organisation_id);
CREATE INDEX idx_newsletter_templates_gym ON newsletter_templates(gym_id);
CREATE INDEX idx_newsletter_templates_active ON newsletter_templates(gym_id, is_active);

-- ============================================================================
-- SECTION 3: RECREATE NEWSLETTER CAMPAIGNS
-- Inherits from GymScopedEntity -> TenantEntity -> BaseAuditEntity
-- ============================================================================

CREATE TABLE newsletter_campaigns (
    -- BaseEntity
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    
    -- TenantEntity columns
    organisation_id UUID NOT NULL,
    
    -- GymScopedEntity columns
    gym_id UUID NOT NULL,
    
    -- NewsletterCampaign specific columns
    template_id UUID,
    name VARCHAR(100),
    subject VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    audience_type VARCHAR(30) NOT NULL,
    audience_filter JSONB,
    scheduled_at TIMESTAMP,
    sent_at TIMESTAMP,
    total_recipients INTEGER DEFAULT 0,
    delivered_count INTEGER DEFAULT 0,
    failed_count INTEGER DEFAULT 0,
    status VARCHAR(20) DEFAULT 'DRAFT',
    sent_by_user_id UUID,

    -- BaseAuditEntity columns
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_newsletter_campaigns_org ON newsletter_campaigns(organisation_id);
CREATE INDEX idx_newsletter_campaigns_gym ON newsletter_campaigns(gym_id);
CREATE INDEX idx_newsletter_campaigns_status ON newsletter_campaigns(gym_id, status);
CREATE INDEX idx_newsletter_campaigns_template ON newsletter_campaigns(template_id);

-- ============================================================================
-- SECTION 4: RECREATE CAMPAIGN RECIPIENTS
-- Inherits from BaseAuditEntity
-- ============================================================================

CREATE TABLE campaign_recipients (
    -- BaseEntity
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    
    -- CampaignRecipient specific columns
    campaign_id UUID NOT NULL,
    member_id UUID NOT NULL,
    email VARCHAR(255) NOT NULL,
    status VARCHAR(20) DEFAULT 'PENDING',
    sent_at TIMESTAMP,
    delivered_at TIMESTAMP,
    error_message TEXT,
    
    -- Multi-channel support columns
    channel_used VARCHAR(20) DEFAULT 'EMAIL',
    fallback_used BOOLEAN DEFAULT FALSE,

    -- BaseAuditEntity columns
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN DEFAULT TRUE
);

CREATE INDEX idx_campaign_recipients_campaign ON campaign_recipients(campaign_id);
CREATE INDEX idx_campaign_recipients_member ON campaign_recipients(member_id);
CREATE INDEX idx_campaign_recipients_status ON campaign_recipients(campaign_id, status);
CREATE INDEX idx_campaign_recipients_channel ON campaign_recipients(channel_used);

-- ============================================================================
-- SECTION 5: FOREIGN KEY CONSTRAINTS
-- ============================================================================

ALTER TABLE newsletter_templates ADD CONSTRAINT fk_newsletter_templates_org 
    FOREIGN KEY (organisation_id) REFERENCES organisations(id) ON DELETE CASCADE;

ALTER TABLE newsletter_templates ADD CONSTRAINT fk_newsletter_templates_gym 
    FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE;

ALTER TABLE newsletter_campaigns ADD CONSTRAINT fk_newsletter_campaigns_org 
    FOREIGN KEY (organisation_id) REFERENCES organisations(id) ON DELETE CASCADE;

ALTER TABLE newsletter_campaigns ADD CONSTRAINT fk_newsletter_campaigns_gym 
    FOREIGN KEY (gym_id) REFERENCES gyms(id) ON DELETE CASCADE;

ALTER TABLE newsletter_campaigns ADD CONSTRAINT fk_newsletter_campaigns_template 
    FOREIGN KEY (template_id) REFERENCES newsletter_templates(id) ON DELETE SET NULL;

ALTER TABLE campaign_recipients ADD CONSTRAINT fk_campaign_recipients_campaign 
    FOREIGN KEY (campaign_id) REFERENCES newsletter_campaigns(id) ON DELETE CASCADE;

ALTER TABLE campaign_recipients ADD CONSTRAINT fk_campaign_recipients_member 
    FOREIGN KEY (member_id) REFERENCES members(id) ON DELETE CASCADE;

-- ============================================================================
-- MIGRATION COMPLETE
-- ============================================================================
