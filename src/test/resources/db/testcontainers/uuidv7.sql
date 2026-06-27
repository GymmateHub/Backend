-- Shim for environments without PostgreSQL 18's native uuidv7().
-- Lets DDL using "DEFAULT uuidv7()" run on postgres:16 in tests.
CREATE OR REPLACE FUNCTION uuidv7() RETURNS uuid
    LANGUAGE sql VOLATILE AS 'SELECT gen_random_uuid()';
