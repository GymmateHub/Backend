-- Rename gym_subscriptions table to subscriptions
ALTER TABLE gym_subscriptions
  RENAME TO subscriptions;

-- Rename gym_id column to organisation_id in subscriptions
-- (Already updated in code, but need to ensure constraint names are correct)
ALTER TABLE subscriptions
  RENAME COLUMN gym_id TO organisation_id;

-- Add unique constraint on organisation_id if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM pg_constraint
        WHERE conname = 'uk_subscriptions_organisation_id'
    ) THEN
        ALTER TABLE subscriptions
          ADD CONSTRAINT uk_subscriptions_organisation_id UNIQUE (organisation_id);
    END IF;
END $$;

-- Rename gym_id column to organisation_id in api_rate_limits
ALTER TABLE api_rate_limits
  RENAME COLUMN gym_id TO organisation_id;

-- Add comment to table
COMMENT ON TABLE subscriptions IS 'Subscription information for organisations (one subscription per organisation covers all gyms)';
COMMENT ON COLUMN subscriptions.organisation_id IS 'Reference to the organisation that owns this subscription';
COMMENT ON COLUMN api_rate_limits.organisation_id IS 'Reference to the organisation being rate limited';

