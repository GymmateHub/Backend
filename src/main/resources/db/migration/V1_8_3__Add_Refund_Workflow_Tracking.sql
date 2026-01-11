-- ============================================
-- GymMate Refund Workflow Enhancement
-- Migration V1.8
-- ============================================

-- ============================================
-- 1. REFUND REQUESTS TABLE
-- Allows members to request refunds, gym owners to approve/reject
-- ============================================
CREATE TABLE IF NOT EXISTS refund_requests (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    gym_id UUID NOT NULL REFERENCES gyms(id) ON DELETE CASCADE,

    -- Request Type
    refund_type VARCHAR(30) NOT NULL, -- PLATFORM_SUBSCRIPTION, MEMBER_PAYMENT

    -- Payment Reference
    stripe_payment_intent_id VARCHAR(255),
    stripe_charge_id VARCHAR(255),
    original_payment_amount DECIMAL(10,2) NOT NULL,
    requested_refund_amount DECIMAL(10,2) NOT NULL,
    currency VARCHAR(3) DEFAULT 'USD',

    -- Related Entities
    membership_id UUID,
    class_booking_id UUID,
    subscription_id UUID REFERENCES gym_subscriptions(id) ON DELETE SET NULL,

    -- Requester Information
    requested_by_user_id UUID NOT NULL, -- The user who requested the refund
    requested_by_type VARCHAR(30) NOT NULL, -- MEMBER, GYM_OWNER, STAFF, SUPER_ADMIN

    -- Recipient Information (who gets the money back)
    refund_to_user_id UUID NOT NULL, -- The user who will receive the refund
    refund_to_type VARCHAR(30) NOT NULL, -- MEMBER, GYM_OWNER

    -- Request Details
    reason_category VARCHAR(50) NOT NULL, -- SERVICE_NOT_PROVIDED, DUPLICATE_CHARGE, DISSATISFIED, CANCELLED_MEMBERSHIP, OTHER
    reason_description TEXT,
    supporting_evidence TEXT, -- URLs to uploaded files/screenshots

    -- Workflow Status
    status VARCHAR(30) NOT NULL DEFAULT 'PENDING', -- PENDING, UNDER_REVIEW, APPROVED, REJECTED, PROCESSED, CANCELLED

    -- Processor Information (who approved/rejected)
    processed_by_user_id UUID,
    processed_by_type VARCHAR(30), -- GYM_OWNER, STAFF, SUPER_ADMIN, SYSTEM
    processed_at TIMESTAMP,
    processor_notes TEXT,
    rejection_reason TEXT,

    -- Link to actual refund once processed
    payment_refund_id UUID,

    -- SLA Tracking
    due_by TIMESTAMP, -- When the refund request should be processed by
    escalated BOOLEAN DEFAULT false,
    escalated_at TIMESTAMP,
    escalated_to VARCHAR(50), -- SUPER_ADMIN, SUPPORT

    -- Metadata
    metadata JSONB,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- 2. UPDATE PAYMENT_REFUNDS TABLE
-- Add recipient and processor tracking
-- ============================================

-- Add refund recipient tracking
ALTER TABLE payment_refunds
    ADD COLUMN IF NOT EXISTS refund_to_user_id UUID,
    ADD COLUMN IF NOT EXISTS refund_to_type VARCHAR(30),
    ADD COLUMN IF NOT EXISTS processed_by_user_id UUID,
    ADD COLUMN IF NOT EXISTS processed_by_type VARCHAR(30),
    ADD COLUMN IF NOT EXISTS refund_request_id UUID,
    ADD COLUMN IF NOT EXISTS refund_type VARCHAR(30) DEFAULT 'PLATFORM_SUBSCRIPTION';

-- Add foreign key for refund request link
ALTER TABLE payment_refunds
    ADD CONSTRAINT fk_payment_refunds_refund_request
    FOREIGN KEY (refund_request_id) REFERENCES refund_requests(id) ON DELETE SET NULL;

-- Update refund_requests with payment_refund link
ALTER TABLE refund_requests
    ADD CONSTRAINT fk_refund_requests_payment_refund
    FOREIGN KEY (payment_refund_id) REFERENCES payment_refunds(id) ON DELETE SET NULL;

-- ============================================
-- 3. REFUND AUDIT LOG TABLE
-- Track all status changes for compliance
-- ============================================
CREATE TABLE IF NOT EXISTS refund_audit_log (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    refund_request_id UUID REFERENCES refund_requests(id) ON DELETE CASCADE,
    payment_refund_id UUID REFERENCES payment_refunds(id) ON DELETE CASCADE,

    -- Change Details
    action VARCHAR(50) NOT NULL, -- CREATED, STATUS_CHANGED, APPROVED, REJECTED, PROCESSED, ESCALATED, CANCELLED
    old_status VARCHAR(30),
    new_status VARCHAR(30),

    -- Actor
    performed_by_user_id UUID,
    performed_by_type VARCHAR(30), -- MEMBER, GYM_OWNER, STAFF, SUPER_ADMIN, SYSTEM

    -- Details
    notes TEXT,
    ip_address VARCHAR(45),
    user_agent TEXT,

    -- Metadata
    metadata JSONB,

    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- ============================================
-- INDEXES FOR PERFORMANCE
-- ============================================

-- Refund Requests
CREATE INDEX idx_refund_requests_gym_id ON refund_requests(gym_id);
CREATE INDEX idx_refund_requests_status ON refund_requests(status);
CREATE INDEX idx_refund_requests_requested_by ON refund_requests(requested_by_user_id);
CREATE INDEX idx_refund_requests_refund_to ON refund_requests(refund_to_user_id);
CREATE INDEX idx_refund_requests_type ON refund_requests(refund_type);
CREATE INDEX idx_refund_requests_created_at ON refund_requests(created_at);
CREATE INDEX idx_refund_requests_due_by ON refund_requests(due_by) WHERE status IN ('PENDING', 'UNDER_REVIEW');
CREATE INDEX idx_refund_requests_escalated ON refund_requests(escalated) WHERE escalated = true;

-- Payment Refunds (new columns)
CREATE INDEX idx_payment_refunds_refund_to ON payment_refunds(refund_to_user_id) WHERE refund_to_user_id IS NOT NULL;
CREATE INDEX idx_payment_refunds_processed_by ON payment_refunds(processed_by_user_id) WHERE processed_by_user_id IS NOT NULL;
CREATE INDEX idx_payment_refunds_refund_type ON payment_refunds(refund_type);

-- Refund Audit Log
CREATE INDEX idx_refund_audit_log_request_id ON refund_audit_log(refund_request_id);
CREATE INDEX idx_refund_audit_log_refund_id ON refund_audit_log(payment_refund_id);
CREATE INDEX idx_refund_audit_log_action ON refund_audit_log(action);
CREATE INDEX idx_refund_audit_log_created_at ON refund_audit_log(created_at);

-- ============================================
-- COMMENTS
-- ============================================

COMMENT ON TABLE refund_requests IS 'Tracks refund requests from members and gym owners with approval workflow';
COMMENT ON TABLE refund_audit_log IS 'Immutable audit trail of all refund-related actions for compliance';
COMMENT ON COLUMN refund_requests.refund_type IS 'PLATFORM_SUBSCRIPTION for gym owner refunds, MEMBER_PAYMENT for member refunds';
COMMENT ON COLUMN refund_requests.status IS 'PENDING: awaiting review, UNDER_REVIEW: being processed, APPROVED: ready to process, REJECTED: denied, PROCESSED: refund completed, CANCELLED: request withdrawn';
COMMENT ON COLUMN payment_refunds.refund_to_user_id IS 'The user who received the refund money';
COMMENT ON COLUMN payment_refunds.processed_by_user_id IS 'The admin/owner who approved and processed the refund';

