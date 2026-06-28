-- Provider-neutral payment schema updates

-- subscriptions
ALTER TABLE IF EXISTS subscriptions
  RENAME COLUMN stripe_subscription_id TO provider_subscription_id;

ALTER TABLE IF EXISTS subscriptions
  RENAME COLUMN stripe_customer_id TO provider_customer_id;

-- subscription tiers
ALTER TABLE IF EXISTS subscription_tiers
  RENAME COLUMN stripe_product_id TO provider_product_id;

ALTER TABLE IF EXISTS subscription_tiers
  RENAME COLUMN stripe_price_id TO provider_plan_id;

-- gym invoices
ALTER TABLE IF EXISTS gym_invoices
  RENAME COLUMN stripe_invoice_id TO provider_invoice_id;

-- payment refunds
ALTER TABLE IF EXISTS payment_refunds
  RENAME COLUMN stripe_refund_id TO provider_refund_id;

ALTER TABLE IF EXISTS payment_refunds
  RENAME COLUMN stripe_payment_intent_id TO provider_transaction_id;

ALTER TABLE IF EXISTS payment_refunds
  RENAME COLUMN stripe_charge_id TO provider_charge_id;

ALTER TABLE IF EXISTS payment_refunds
  RENAME COLUMN stripe_created_at TO provider_created_at;

-- refund requests
ALTER TABLE IF EXISTS refund_requests
  RENAME COLUMN stripe_payment_intent_id TO provider_transaction_id;

ALTER TABLE IF EXISTS refund_requests
  RENAME COLUMN stripe_charge_id TO provider_charge_id;

-- webhook events
ALTER TABLE IF EXISTS stripe_webhook_events
  RENAME TO payment_webhook_events;

ALTER TABLE IF EXISTS payment_webhook_events
  RENAME COLUMN stripe_event_id TO provider_event_id;

ALTER TABLE IF EXISTS payment_webhook_events
  ADD COLUMN IF NOT EXISTS provider VARCHAR(30);

UPDATE payment_webhook_events
SET provider = COALESCE(provider, 'stripe')
WHERE provider IS NULL;

ALTER TABLE payment_webhook_events
  ALTER COLUMN provider SET NOT NULL;

-- refresh indexes for renamed columns
DROP INDEX IF EXISTS idx_gi_stripe_invoice;
CREATE INDEX IF NOT EXISTS idx_gi_provider_invoice ON gym_invoices(provider_invoice_id);

DROP INDEX IF EXISTS idx_pr_stripe_refund;
CREATE INDEX IF NOT EXISTS idx_pr_provider_refund ON payment_refunds(provider_refund_id);

