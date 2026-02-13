-- V5: Admin Notification System
-- Adds notifications table for real-time admin notifications via SSE

-- Create uuid-ossp extension if not exists (for uuidv7)
CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS notifications (
    id UUID PRIMARY KEY DEFAULT uuidv7(),
    organisation_id UUID NOT NULL,
    title VARCHAR(200) NOT NULL,
    message TEXT NOT NULL,
    priority VARCHAR(20) NOT NULL DEFAULT 'MEDIUM',
    event_type VARCHAR(50) NOT NULL,
    metadata JSONB,
    related_entity_id UUID,
    related_entity_type VARCHAR(50),
    recipient_role VARCHAR(20),
    read_at TIMESTAMP,
    delivered_via VARCHAR(20),
    delivered_at TIMESTAMP,

    -- Audit fields (from BaseAuditEntity)
    created_by VARCHAR(255),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_by VARCHAR(255),
    is_active BOOLEAN NOT NULL DEFAULT true,

    CONSTRAINT fk_notification_organisation FOREIGN KEY (organisation_id)
        REFERENCES organisations(id) ON DELETE CASCADE
);

-- Indexes for performance
CREATE INDEX idx_notifications_org_unread ON notifications(organisation_id, read_at)
    WHERE read_at IS NULL;

CREATE INDEX idx_notifications_org_created ON notifications(organisation_id, created_at DESC);

CREATE INDEX idx_notifications_event_type ON notifications(event_type);

CREATE INDEX idx_notifications_priority ON notifications(priority);

-- Comments
COMMENT ON TABLE notifications IS 'Admin notifications for real-time dashboard updates via SSE';
COMMENT ON COLUMN notifications.organisation_id IS 'Organisation this notification belongs to';
COMMENT ON COLUMN notifications.title IS 'Short notification title for display';
COMMENT ON COLUMN notifications.message IS 'Full notification message';
COMMENT ON COLUMN notifications.priority IS 'Priority level: CRITICAL, HIGH, MEDIUM, LOW';
COMMENT ON COLUMN notifications.event_type IS 'Type of event: PAYMENT_FAILED, PAYMENT_SUCCESS, etc.';
COMMENT ON COLUMN notifications.metadata IS 'JSON metadata with event-specific details';
COMMENT ON COLUMN notifications.related_entity_id IS 'ID of related entity (gym, member, subscription, etc.)';
COMMENT ON COLUMN notifications.related_entity_type IS 'Type of related entity (GYM, MEMBER, SUBSCRIPTION, etc.)';
COMMENT ON COLUMN notifications.recipient_role IS 'Target recipient role: OWNER, ADMIN, SUPER_ADMIN';
COMMENT ON COLUMN notifications.read_at IS 'Timestamp when notification was marked as read';
COMMENT ON COLUMN notifications.delivered_via IS 'Delivery channel used: EMAIL, SSE, BOTH';
COMMENT ON COLUMN notifications.delivered_at IS 'Timestamp when notification was delivered';

