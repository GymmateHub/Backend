-- V8: Add GYM_OWNER and MANAGER roles
-- Migrate existing OWNER users to GYM_OWNER.
-- MANAGER is a new role — no existing rows affected.

UPDATE users SET role = 'GYM_OWNER' WHERE role = 'OWNER';
