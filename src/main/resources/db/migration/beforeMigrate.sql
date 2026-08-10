-- Flyway beforeMigrate callback: runs before every migrate.
--
-- PostgreSQL 18+ provides native uuidv7() support.
-- If running on PostgreSQL < 18, create a PL/pgSQL uuidv7() fallback if missing.
DO $$
BEGIN
    IF NOT EXISTS (SELECT 1 FROM pg_proc WHERE proname = 'uuidv7') THEN
        CREATE OR REPLACE FUNCTION public.uuidv7() RETURNS uuid AS $func$
        DECLARE
            unix_time_ms bytea;
            rand_bytes bytea;
        BEGIN
            unix_time_ms := substring(int8send(floor(extract(epoch FROM clock_timestamp()) * 1000)::bigint) FROM 3);
            rand_bytes := gen_random_bytes(10);
            rand_bytes := set_byte(rand_bytes, 0, (get_byte(rand_bytes, 0) & 15) | 112);
            rand_bytes := set_byte(rand_bytes, 2, (get_byte(rand_bytes, 2) & 63) | 128);
            RETURN encode(unix_time_ms || rand_bytes, 'hex')::uuid;
        END;
        $func$ LANGUAGE plpgsql VOLATILE;
    END IF;
END $$;


