-- Flyway beforeMigrate callback: runs before every migrate.
--
-- The schema (V1__Complete_Schema.sql onward) declares UUID primary keys with
-- DEFAULT uuidv7(). uuidv7() is only built-in on PostgreSQL 18+, so on older
-- servers (the VPS runs PostgreSQL 17) the very first migration fails with
-- "function uuidv7() does not exist". Provide a pure-SQL implementation so the
-- app self-migrates on any PostgreSQL >= 13 without a manual DB shim.
--
-- CREATE OR REPLACE is idempotent and harmless on PostgreSQL 18 (it simply
-- shadows the native function in the public schema with an equivalent one).
CREATE OR REPLACE FUNCTION public.uuidv7() RETURNS uuid AS $$
  SELECT encode(
    set_bit(
      set_bit(
        overlay(uuid_send(gen_random_uuid())
                PLACING substring(int8send(floor(extract(epoch FROM clock_timestamp()) * 1000)::bigint) FROM 3)
                FROM 1 FOR 6),
        52, 1),
      53, 1),
    'hex')::uuid;
$$ LANGUAGE sql VOLATILE;
