-- ============================================================================
-- Booking System Concurrency Enhancement Migration
-- Version: 2.0.0
-- Date: January 2024
-- 
-- Purpose: Add optimistic locking and performance indexes for enterprise-grade
--          booking concurrency control (Uber/Ola style)
-- ============================================================================

-- Step 1: Backup current booking data (recommended before running)
-- pg_dump ankurshala_db -t booking > backup_booking_$(date +%Y%m%d).sql

BEGIN;

-- Step 2: Add version column for optimistic locking
-- This enables JPA to detect concurrent updates automatically
ALTER TABLE bookings 
ADD COLUMN IF NOT EXISTS version BIGINT NOT NULL DEFAULT 0;

-- Step 3: Create indexes for high-performance booking queries

-- Index for finding pending bookings (used by expiration service)
CREATE INDEX IF NOT EXISTS idx_bookings_status_created 
ON bookings(status, created_at) 
WHERE status = 'REQUESTED' AND teacher_id IS NULL;

-- Index for atomic booking acceptance queries
CREATE INDEX IF NOT EXISTS idx_bookings_acceptance 
ON bookings(id, status, teacher_id) 
WHERE status = 'REQUESTED' AND teacher_id IS NULL;

-- Index for teacher conflict checking
CREATE INDEX IF NOT EXISTS idx_bookings_teacher_time 
ON bookings(teacher_id, start_time, end_time, status)
WHERE status IN ('REQUESTED', 'ACCEPTED');

-- Index for student booking history
CREATE INDEX IF NOT EXISTS idx_bookings_student_time 
ON bookings(student_id, start_time, status);

-- Composite index for calendar queries
CREATE INDEX IF NOT EXISTS idx_bookings_calendar 
ON bookings(student_id, teacher_id, start_time, status)
WHERE status IN ('REQUESTED', 'ACCEPTED');

-- Step 4: Add performance optimization hints
-- Increase statistics target for better query planning
ALTER TABLE bookings ALTER COLUMN status SET STATISTICS 1000;
ALTER TABLE bookings ALTER COLUMN start_time SET STATISTICS 1000;
ALTER TABLE bookings ALTER COLUMN created_at SET STATISTICS 1000;

-- Step 5: Update table statistics for query optimizer
ANALYZE bookings;

-- Step 6: Create function for booking acceptance metrics (optional)
CREATE OR REPLACE FUNCTION get_booking_acceptance_metrics(days_back INTEGER DEFAULT 7)
RETURNS TABLE (
    total_bookings BIGINT,
    accepted_bookings BIGINT,
    expired_bookings BIGINT,
    acceptance_rate NUMERIC,
    expiration_rate NUMERIC,
    avg_acceptance_time_minutes NUMERIC
) AS $$
BEGIN
    RETURN QUERY
    SELECT 
        COUNT(*) as total_bookings,
        COUNT(CASE WHEN status = 'ACCEPTED' THEN 1 END) as accepted_bookings,
        COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) as expired_bookings,
        ROUND(COUNT(CASE WHEN status = 'ACCEPTED' THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0), 2) as acceptance_rate,
        ROUND(COUNT(CASE WHEN status = 'CANCELLED' THEN 1 END) * 100.0 / NULLIF(COUNT(*), 0), 2) as expiration_rate,
        ROUND(AVG(EXTRACT(EPOCH FROM (accepted_at - created_at)) / 60), 2) as avg_acceptance_time_minutes
    FROM bookings
    WHERE created_at > NOW() - INTERVAL '1 day' * days_back;
END;
$$ LANGUAGE plpgsql;

-- Step 7: Create view for monitoring booking conflicts (optional)
CREATE OR REPLACE VIEW booking_conflicts AS
SELECT 
    b1.id as booking1_id,
    b2.id as booking2_id,
    b1.teacher_id,
    b1.start_time as booking1_start,
    b1.end_time as booking1_end,
    b2.start_time as booking2_start,
    b2.end_time as booking2_end,
    b1.status as booking1_status,
    b2.status as booking2_status
FROM bookings b1
JOIN bookings b2 ON b1.teacher_id = b2.teacher_id 
    AND b1.id < b2.id
    AND b1.start_time < b2.end_time 
    AND b1.end_time > b2.start_time
WHERE b1.status IN ('REQUESTED', 'ACCEPTED')
  AND b2.status IN ('REQUESTED', 'ACCEPTED');

-- Step 8: Add comment documentation
COMMENT ON COLUMN bookings.version IS 'Optimistic locking version for concurrent update detection';
COMMENT ON INDEX idx_bookings_status_created IS 'Performance index for booking expiration queries';
COMMENT ON INDEX idx_bookings_acceptance IS 'Performance index for atomic booking acceptance';
COMMENT ON INDEX idx_bookings_teacher_time IS 'Performance index for teacher time conflict detection';
COMMENT ON FUNCTION get_booking_acceptance_metrics IS 'Calculate booking acceptance and expiration metrics';
COMMENT ON VIEW booking_conflicts IS 'Monitor potential booking conflicts for quality assurance';

COMMIT;

-- ============================================================================
-- Verification Queries
-- ============================================================================

-- Verify version column added
SELECT column_name, data_type, is_nullable, column_default 
FROM information_schema.columns 
WHERE table_name = 'bookings' AND column_name = 'version';

-- Verify indexes created
SELECT indexname, indexdef 
FROM pg_indexes 
WHERE tablename = 'bookings' 
AND indexname LIKE 'idx_bookings_%'
ORDER BY indexname;

-- Check table statistics
SELECT attname, n_distinct, correlation 
FROM pg_stats 
WHERE tablename = 'bookings' 
AND attname IN ('status', 'start_time', 'created_at', 'teacher_id');

-- Test booking metrics function
SELECT * FROM get_booking_acceptance_metrics(7);

-- Check for existing conflicts (should be 0)
SELECT COUNT(*) as conflict_count FROM booking_conflicts;

-- ============================================================================
-- Performance Verification
-- ============================================================================

-- Test query 1: Find pending bookings (should use idx_bookings_status_created)
EXPLAIN ANALYZE
SELECT * FROM bookings 
WHERE status = 'REQUESTED' AND teacher_id IS NULL 
ORDER BY created_at DESC 
LIMIT 100;

-- Test query 2: Atomic acceptance (should use idx_bookings_acceptance)
EXPLAIN ANALYZE
SELECT * FROM bookings 
WHERE id = 1 AND status = 'REQUESTED' AND teacher_id IS NULL;

-- Test query 3: Teacher time conflicts (should use idx_bookings_teacher_time)
EXPLAIN ANALYZE
SELECT * FROM bookings 
WHERE teacher_id = 1 
AND start_time < NOW() + INTERVAL '2 hours'
AND end_time > NOW() + INTERVAL '1 hour'
AND status IN ('REQUESTED', 'ACCEPTED');

-- ============================================================================
-- Rollback Script (in case of issues)
-- ============================================================================

-- Uncomment and run if rollback needed:
/*
BEGIN;

DROP VIEW IF EXISTS booking_conflicts;
DROP FUNCTION IF EXISTS get_booking_acceptance_metrics(INTEGER);

DROP INDEX IF EXISTS idx_bookings_calendar;
DROP INDEX IF EXISTS idx_bookings_student_time;
DROP INDEX IF EXISTS idx_bookings_teacher_time;
DROP INDEX IF EXISTS idx_bookings_acceptance;
DROP INDEX IF EXISTS idx_bookings_status_created;

ALTER TABLE bookings DROP COLUMN IF EXISTS version;

COMMIT;

-- Restore from backup:
-- psql ankurshala_db < backup_booking_YYYYMMDD.sql
*/

-- ============================================================================
-- Success Confirmation
-- ============================================================================

DO $$
DECLARE
    version_exists BOOLEAN;
    index_count INTEGER;
BEGIN
    -- Check version column
    SELECT EXISTS(
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'bookings' AND column_name = 'version'
    ) INTO version_exists;
    
    -- Count indexes
    SELECT COUNT(*) INTO index_count
    FROM pg_indexes 
    WHERE tablename = 'bookings' AND indexname LIKE 'idx_bookings_%';
    
    -- Report results
    IF version_exists AND index_count >= 5 THEN
        RAISE NOTICE '✅ Migration completed successfully!';
        RAISE NOTICE '✅ Version column added';
        RAISE NOTICE '✅ % performance indexes created', index_count;
        RAISE NOTICE '✅ Booking system ready for enterprise-grade concurrency';
    ELSE
        RAISE WARNING '⚠️  Migration may have issues:';
        IF NOT version_exists THEN
            RAISE WARNING '❌ Version column not found';
        END IF;
        IF index_count < 5 THEN
            RAISE WARNING '⚠️  Only % indexes created (expected 5+)', index_count;
        END IF;
    END IF;
END $$;
