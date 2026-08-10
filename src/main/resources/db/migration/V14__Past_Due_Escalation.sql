-- Supports grace-period escalation of PAST_DUE subscriptions/memberships to SUSPENDED
-- (SubscriptionService.escalatePastDueSubscriptions / MembershipService.escalatePastDueMemberships).
-- Without a timestamp of when PAST_DUE started, the escalation job has no way to tell
-- "just failed" from "failed a week ago" — both status columns already allow arbitrary
-- VARCHAR values (no CHECK constraint), so no other schema change is needed to
-- introduce the MembershipStatus.SUSPENDED value on the member_memberships side.

ALTER TABLE subscriptions
    ADD COLUMN IF NOT EXISTS past_due_since TIMESTAMP;

ALTER TABLE member_memberships
    ADD COLUMN IF NOT EXISTS past_due_since TIMESTAMP;

-- Backfill: anything already PAST_DUE today has its clock start now rather than being
-- treated as indefinitely fresh (NULL) or immediately stale (an arbitrary past date).
UPDATE subscriptions SET past_due_since = CURRENT_TIMESTAMP
 WHERE status = 'PAST_DUE' AND past_due_since IS NULL;

UPDATE member_memberships SET past_due_since = CURRENT_TIMESTAMP
 WHERE status = 'PAST_DUE' AND past_due_since IS NULL;
