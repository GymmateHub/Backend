-- V6: Add Gym-Level Notification Support
-- Extends notifications table to support gym-specific notifications alongside organisation-level notifications

-- Add gym_id column for gym-level notification targeting
ALTER TABLE notifications
  ADD COLUMN IF NOT EXISTS gym_id UUID;

-- Add notification_scope column to distinguish between organisation and gym-scoped notifications
ALTER TABLE notifications
  ADD COLUMN IF NOT EXISTS notification_scope VARCHAR(20) DEFAULT 'ORGANISATION';

-- Add gym-level indexes for fast unread queries
CREATE INDEX IF NOT EXISTS idx_notifications_gym_unread ON notifications (gym_id, read_at)
  WHERE gym_id IS NOT NULL AND read_at IS NULL;

-- Add gym-level chronological index
CREATE INDEX IF NOT EXISTS idx_notifications_gym_created ON notifications (gym_id, created_at DESC)
  WHERE gym_id IS NOT NULL;

-- Add scope + organisation index for mixed queries
CREATE INDEX IF NOT EXISTS idx_notifications_scope ON notifications (notification_scope, organisation_id);

-- Add foreign key constraint for gym_id referencing gyms table
DO
$$
  BEGIN
    IF NOT EXISTS (SELECT 1
                   FROM pg_constraint c
                   WHERE c.conname = 'fk_notification_gym') THEN
      ALTER TABLE notifications
        ADD CONSTRAINT fk_notification_gym
          FOREIGN KEY (gym_id) REFERENCES gyms (id) ON DELETE CASCADE;
    END IF;
  END
$$;

-- Add comments for documentation
COMMENT ON COLUMN notifications.gym_id IS 'Specific gym this notification targets (null if organisation-wide)';
COMMENT ON COLUMN notifications.notification_scope IS 'Notification scope: ORGANISATION (all staff) or GYM (specific gym only)';

