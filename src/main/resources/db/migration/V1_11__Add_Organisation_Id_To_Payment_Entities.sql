-- =====================================================
-- V1.11: Add Organisation ID to Payment Entities
-- =====================================================
-- This migration adds organisation_id to payment-related entities
-- for proper multi-tenant billing support.

-- 1. Add organisation_id to payment_methods table
ALTER TABLE payment_methods ADD COLUMN IF NOT EXISTS organisation_id UUID;
CREATE INDEX IF NOT EXISTS idx_payment_methods_organisation ON payment_methods(organisation_id);

-- 2. Add organisation_id to payment_refunds table
ALTER TABLE payment_refunds ADD COLUMN IF NOT EXISTS organisation_id UUID;
CREATE INDEX IF NOT EXISTS idx_payment_refunds_organisation ON payment_refunds(organisation_id);

-- 3. Add organisation_id to gym_invoices table
ALTER TABLE gym_invoices ADD COLUMN IF NOT EXISTS organisation_id UUID;
CREATE INDEX IF NOT EXISTS idx_gym_invoices_organisation ON gym_invoices(organisation_id);

-- 4. Make gym_id nullable in payment_methods (was required before)
-- This allows payment methods to be owned by organisation directly
ALTER TABLE payment_methods ALTER COLUMN gym_id DROP NOT NULL;

-- 5. Make gym_id nullable in gym_invoices (invoices are now at org level)
ALTER TABLE gym_invoices ALTER COLUMN gym_id DROP NOT NULL;

-- 6. Make gym_id nullable in payment_refunds (refunds are now at org level)
ALTER TABLE payment_refunds ALTER COLUMN gym_id DROP NOT NULL;

-- 7. Data migration: Populate organisation_id from gym.organisation_id for existing records
-- Payment Methods
UPDATE payment_methods pm
SET organisation_id = g.organisation_id
FROM gyms g
WHERE pm.gym_id = g.id
  AND pm.organisation_id IS NULL;

-- Payment Refunds
UPDATE payment_refunds pr
SET organisation_id = g.organisation_id
FROM gyms g
WHERE pr.gym_id = g.id
  AND pr.organisation_id IS NULL;

-- Gym Invoices
UPDATE gym_invoices gi
SET organisation_id = g.organisation_id
FROM gyms g
WHERE gi.gym_id = g.id
  AND gi.organisation_id IS NULL;

-- 8. Comments for documentation
COMMENT ON COLUMN payment_methods.organisation_id IS 'Organisation (billing entity) that owns this payment method';
COMMENT ON COLUMN payment_refunds.organisation_id IS 'Organisation (billing entity) this refund belongs to';
COMMENT ON COLUMN gym_invoices.organisation_id IS 'Organisation (billing entity) this invoice belongs to';

