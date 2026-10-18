-- Spring Modulith 2.x event publication registry: publications now carry a lifecycle status,
-- the number of completion attempts and the last resubmission time (mirrors
-- spring-modulith-events-jdbc 2.1 schemas/v2/schema-postgresql.sql).
ALTER TABLE event_publication
    ADD COLUMN IF NOT EXISTS status                 TEXT,
    ADD COLUMN IF NOT EXISTS completion_attempts    INT,
    ADD COLUMN IF NOT EXISTS last_resubmission_date TIMESTAMP WITH TIME ZONE;

-- Publications recorded by Modulith 1.x: completed ones are done, the rest are still pending.
UPDATE event_publication
   SET status = CASE WHEN completion_date IS NOT NULL THEN 'COMPLETED' ELSE 'PUBLISHED' END
 WHERE status IS NULL;

UPDATE event_publication
   SET completion_attempts = 0
 WHERE completion_attempts IS NULL;
