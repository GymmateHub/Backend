-- ============================================================================
-- V11: Standardise the gym owner role name on OWNER (reverses V8)
-- The codebase's @PreAuthorize checks and the frontend both use OWNER;
-- UserRole.GYM_OWNER has been renamed to UserRole.OWNER.
-- ============================================================================

UPDATE users SET role = 'OWNER' WHERE role = 'GYM_OWNER';

-- Defensive: invites should never hold GYM_OWNER (blocked by InviteService),
-- but normalise any legacy rows just in case
UPDATE user_invites SET role = 'OWNER' WHERE role = 'GYM_OWNER';
