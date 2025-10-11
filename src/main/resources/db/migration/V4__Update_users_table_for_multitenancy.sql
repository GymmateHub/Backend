-- Drop old indexes
DROP INDEX IF EXISTS idx_users_email;
DROP INDEX IF EXISTS idx_users_role;
DROP INDEX IF EXISTS idx_users_status;
DROP INDEX IF EXISTS idx_users_created_at;

-- Create new composite indexes for multi-tenancy
CREATE INDEX idx_users_gym_role ON users(gym_id, role);
CREATE INDEX idx_users_gym_status ON users(gym_id, status);
CREATE INDEX idx_users_gym_created ON users(gym_id, created_at);

-- Create unique indexes for email within gym context
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_gym ON users(email, gym_id) WHERE gym_id IS NOT NULL;
CREATE UNIQUE INDEX IF NOT EXISTS idx_users_email_owner ON users(email) WHERE role = 'GYM_OWNER';

-- Add constraints for multi-tenancy validation
ALTER TABLE users ADD CONSTRAINT chk_gym_owner_no_gym_id
    CHECK ((role = 'GYM_OWNER' AND gym_id IS NULL) OR role != 'GYM_OWNER');

ALTER TABLE users ADD CONSTRAINT chk_member_has_gym_id
    CHECK ((role = 'MEMBER' AND gym_id IS NOT NULL) OR role != 'MEMBER');

ALTER TABLE users ADD CONSTRAINT chk_staff_has_gym_id
    CHECK ((role = 'STAFF' AND gym_id IS NOT NULL) OR role != 'STAFF');

-- Update role enum to include STAFF
ALTER TABLE users DROP CONSTRAINT IF EXISTS users_role_check;
ALTER TABLE users ADD CONSTRAINT users_role_check
    CHECK (role IN ('MEMBER', 'STAFF', 'GYM_OWNER', 'ADMIN'));
