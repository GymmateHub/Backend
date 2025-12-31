-- ============================================
-- PostgreSQL Schema Export Script for GymMate
-- Run this in your PostgreSQL database to get the current schema
-- ============================================

-- 1. Get all table names and their columns
SELECT
    t.table_name,
    c.column_name,
    c.data_type,
    c.character_maximum_length,
    c.is_nullable,
    c.column_default
FROM information_schema.tables t
JOIN information_schema.columns c ON t.table_name = c.table_name
WHERE t.table_schema = 'public'
    AND t.table_type = 'BASE TABLE'
ORDER BY t.table_name, c.ordinal_position;

-- 2. Get just table names
SELECT table_name
FROM information_schema.tables
WHERE table_schema = 'public'
    AND table_type = 'BASE TABLE'
ORDER BY table_name;

-- 3. Get specific table structure (payment_methods example)
SELECT
    column_name,
    data_type,
    is_nullable,
    column_default
FROM information_schema.columns
WHERE table_name = 'payment_methods'
ORDER BY ordinal_position;

-- 4. Get all indexes
SELECT
    tablename,
    indexname,
    indexdef
FROM pg_indexes
WHERE schemaname = 'public'
ORDER BY tablename, indexname;

-- 5. Get all foreign key constraints
SELECT
    tc.table_name,
    kcu.column_name,
    ccu.table_name AS foreign_table_name,
    ccu.column_name AS foreign_column_name
FROM information_schema.table_constraints AS tc
JOIN information_schema.key_column_usage AS kcu
    ON tc.constraint_name = kcu.constraint_name
    AND tc.table_schema = kcu.table_schema
JOIN information_schema.constraint_column_usage AS ccu
    ON ccu.constraint_name = tc.constraint_name
    AND ccu.table_schema = tc.table_schema
WHERE tc.constraint_type = 'FOREIGN KEY'
    AND tc.table_schema = 'public';

-- 6. Get Flyway migration history
SELECT * FROM flyway_schema_history ORDER BY installed_rank;

-- 7. Export full schema DDL (requires pg_dump, run from command line)
-- pg_dump --schema-only --no-owner --no-privileges -h <host> -p <port> -U <user> -d gymmate_db > schema_export.sql

-- 8. Quick check for tables we expect vs what exists
SELECT
    CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'payment_methods')
        THEN 'EXISTS' ELSE 'MISSING'
    END AS payment_methods,
    CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'gym_payment_methods')
        THEN 'EXISTS' ELSE 'MISSING'
    END AS gym_payment_methods,
    CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'refund_requests')
        THEN 'EXISTS' ELSE 'MISSING'
    END AS refund_requests,
    CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'refund_audit_log')
        THEN 'EXISTS' ELSE 'MISSING'
    END AS refund_audit_log,
    CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'payment_refunds')
        THEN 'EXISTS' ELSE 'MISSING'
    END AS payment_refunds,
    CASE
        WHEN EXISTS (SELECT 1 FROM information_schema.tables WHERE table_name = 'refunds')
        THEN 'EXISTS' ELSE 'MISSING'
    END AS refunds;

