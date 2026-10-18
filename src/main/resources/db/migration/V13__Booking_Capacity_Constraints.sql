-- Closes the overbooking race in ClassBookingService.createBooking: the previous
-- implementation was a plain read-then-write capacity check with no DB-level guarantee
-- (classic TOCTOU). Two layers added here:
--
-- 1. booked_count on class_schedules, incremented/decremented atomically by the
--    application via a conditional UPDATE, enforced server-side by a trigger since
--    effective capacity is either capacity_override on the schedule itself or the
--    parent class's capacity (classes.capacity) — a value living in another table, so
--    a plain column CHECK constraint can't express it.
-- 2. A partial unique index closing the existsByClassScheduleIdAndMemberId TOCTOU
--    (duplicate active booking for the same member/schedule).

ALTER TABLE class_schedules
    ADD COLUMN IF NOT EXISTS booked_count INTEGER NOT NULL DEFAULT 0;

CREATE OR REPLACE FUNCTION enforce_class_schedule_capacity()
RETURNS TRIGGER AS $$
DECLARE
    effective_capacity INTEGER;
BEGIN
    IF NEW.booked_count < 0 THEN
        RAISE EXCEPTION 'booked_count cannot be negative for class_schedule %', NEW.id
            USING ERRCODE = '23514';
    END IF;

    SELECT COALESCE(NEW.capacity_override, c.capacity)
      INTO effective_capacity
      FROM classes c
     WHERE c.id = NEW.class_id;

    IF effective_capacity IS NOT NULL AND NEW.booked_count > effective_capacity THEN
        RAISE EXCEPTION 'booked_count % exceeds capacity % for class_schedule %',
            NEW.booked_count, effective_capacity, NEW.id
            USING ERRCODE = '23514';
    END IF;

    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

DROP TRIGGER IF EXISTS trg_class_schedule_capacity ON class_schedules;
CREATE TRIGGER trg_class_schedule_capacity
    BEFORE INSERT OR UPDATE OF booked_count ON class_schedules
    FOR EACH ROW EXECUTE FUNCTION enforce_class_schedule_capacity();

-- Backfill booked_count for existing schedules from current confirmed bookings.
UPDATE class_schedules cs
   SET booked_count = sub.confirmed_count
  FROM (
        SELECT class_schedule_id, COUNT(*) AS confirmed_count
          FROM class_bookings
         WHERE status = 'CONFIRMED'
         GROUP BY class_schedule_id
       ) sub
 WHERE cs.id = sub.class_schedule_id;

-- One active (CONFIRMED/WAITLISTED) booking per member per schedule.
CREATE UNIQUE INDEX IF NOT EXISTS uq_class_bookings_active_member_schedule
    ON class_bookings (class_schedule_id, member_id)
    WHERE status IN ('CONFIRMED', 'WAITLISTED');
