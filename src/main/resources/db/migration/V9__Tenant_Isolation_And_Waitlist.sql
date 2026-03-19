-- ============================================================================
-- V9: Tenant isolation for trainers/staff + waitlist position tracking
-- ============================================================================

-- 1. Add organisation_id to trainers table for tenant isolation
ALTER TABLE trainers ADD COLUMN IF NOT EXISTS organisation_id UUID;
ALTER TABLE trainers ADD COLUMN IF NOT EXISTS updated_by UUID;

-- Backfill organisation_id from linked user
UPDATE trainers t
SET organisation_id = u.organisation_id
FROM users u
WHERE t.user_id = u.id
  AND t.organisation_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_trainers_organisation ON trainers(organisation_id);

-- 2. Add organisation_id to staff table for tenant isolation
ALTER TABLE staff ADD COLUMN IF NOT EXISTS organisation_id UUID;
ALTER TABLE staff ADD COLUMN IF NOT EXISTS updated_by UUID;

-- Backfill organisation_id from linked user
UPDATE staff s
SET organisation_id = u.organisation_id
FROM users u
WHERE s.user_id = u.id
  AND s.organisation_id IS NULL;

CREATE INDEX IF NOT EXISTS idx_staff_organisation ON staff(organisation_id);

-- 3. Add waitlist_position to class_bookings for waitlist management
ALTER TABLE class_bookings ADD COLUMN IF NOT EXISTS waitlist_position INTEGER;

CREATE INDEX IF NOT EXISTS idx_class_bookings_waitlist
  ON class_bookings(class_schedule_id, waitlist_position)
  WHERE status = 'WAITLISTED';

-- 4. Add expired membership tracking — index for scheduled expiry job
CREATE INDEX IF NOT EXISTS idx_member_memberships_expiry
  ON member_memberships(status, end_date)
  WHERE status = 'ACTIVE';

