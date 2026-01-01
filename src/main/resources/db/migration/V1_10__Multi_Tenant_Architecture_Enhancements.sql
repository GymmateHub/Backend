-- =====================================================
-- V1.10: Multi-Tenant Architecture Enhancements
-- =====================================================
-- This migration adds gym_id to gym-scoped entities and
-- adds organisation_id where missing for tenant filtering.

-- 1. Add gym_id to classes table (if not exists)
ALTER TABLE classes ADD COLUMN IF NOT EXISTS gym_id UUID;
CREATE INDEX IF NOT EXISTS idx_classes_gym_id ON classes(gym_id);
CREATE INDEX IF NOT EXISTS idx_classes_organisation_id ON classes(organisation_id);

-- 2. Add gym_id to class_schedules table (if not exists)
ALTER TABLE class_schedules ADD COLUMN IF NOT EXISTS gym_id UUID;
CREATE INDEX IF NOT EXISTS idx_class_schedules_gym_id ON class_schedules(gym_id);
CREATE INDEX IF NOT EXISTS idx_class_schedules_organisation_id ON class_schedules(organisation_id);

-- 3. Add gym_id to class_bookings table (if not exists)
ALTER TABLE class_bookings ADD COLUMN IF NOT EXISTS gym_id UUID;
CREATE INDEX IF NOT EXISTS idx_class_bookings_gym_id ON class_bookings(gym_id);
CREATE INDEX IF NOT EXISTS idx_class_bookings_organisation_id ON class_bookings(organisation_id);

-- 4. Add organisation_id to class_categories table (if not exists)
ALTER TABLE class_categories ADD COLUMN IF NOT EXISTS organisation_id UUID;
CREATE INDEX IF NOT EXISTS idx_class_categories_gym_id ON class_categories(gym_id);
CREATE INDEX IF NOT EXISTS idx_class_categories_organisation_id ON class_categories(organisation_id);

-- 5. Add organisation_id to gym_areas table (if not exists)
ALTER TABLE gym_areas ADD COLUMN IF NOT EXISTS organisation_id UUID;
CREATE INDEX IF NOT EXISTS idx_gym_areas_gym_id ON gym_areas(gym_id);
CREATE INDEX IF NOT EXISTS idx_gym_areas_organisation_id ON gym_areas(organisation_id);

-- 6. Add gym_id to member_memberships table (if not exists)
ALTER TABLE member_memberships ADD COLUMN IF NOT EXISTS gym_id UUID;
CREATE INDEX IF NOT EXISTS idx_member_memberships_gym_id ON member_memberships(gym_id);
CREATE INDEX IF NOT EXISTS idx_member_memberships_organisation_id ON member_memberships(organisation_id);

-- 7. Add gym_id to membership_plans table (if not exists)
-- Note: gym_id may already exist, this ensures organisation_id is also present
ALTER TABLE membership_plans ADD COLUMN IF NOT EXISTS organisation_id UUID;
CREATE INDEX IF NOT EXISTS idx_membership_plans_gym_id ON membership_plans(gym_id);
CREATE INDEX IF NOT EXISTS idx_membership_plans_organisation_id ON membership_plans(organisation_id);

-- 8. Add gym_id to member_payment_methods table (if not exists)
ALTER TABLE member_payment_methods ADD COLUMN IF NOT EXISTS gym_id UUID;
CREATE INDEX IF NOT EXISTS idx_member_payment_methods_gym_id ON member_payment_methods(gym_id);
CREATE INDEX IF NOT EXISTS idx_member_payment_methods_organisation_id ON member_payment_methods(organisation_id);

-- 9. Add gym_id to member_invoices table (if not exists)
ALTER TABLE member_invoices ADD COLUMN IF NOT EXISTS gym_id UUID;
CREATE INDEX IF NOT EXISTS idx_member_invoices_gym_id ON member_invoices(gym_id);
CREATE INDEX IF NOT EXISTS idx_member_invoices_organisation_id ON member_invoices(organisation_id);

-- 10. Add organisation_id to members table (if not exists)
ALTER TABLE members ADD COLUMN IF NOT EXISTS organisation_id UUID;
CREATE INDEX IF NOT EXISTS idx_members_organisation_id ON members(organisation_id);

-- 11. Add index on gyms.organisation_id (if not exists)
CREATE INDEX IF NOT EXISTS idx_gyms_organisation_id ON gyms(organisation_id);

-- 12. Comments for documentation
COMMENT ON COLUMN classes.gym_id IS 'Gym this class belongs to';
COMMENT ON COLUMN classes.organisation_id IS 'Organisation for tenant filtering';
COMMENT ON COLUMN class_schedules.gym_id IS 'Gym this schedule belongs to';
COMMENT ON COLUMN class_bookings.gym_id IS 'Gym where booking was made';
COMMENT ON COLUMN members.organisation_id IS 'Organisation for tenant filtering';
COMMENT ON COLUMN member_memberships.gym_id IS 'Gym where membership is active';

