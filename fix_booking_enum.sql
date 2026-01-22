-- Fix booking_status enum to match Java enum
-- The Java code uses PENDING, CONFIRMED, IN_PROGRESS, REFUNDED, etc.
-- But the database enum (from V14) only has REQUESTED, ACCEPTED, RESCHEDULED, etc.

-- Add missing enum values
DO $$
BEGIN
    -- Add PENDING (same as REQUESTED conceptually)
    IF NOT EXISTS (SELECT 1 FROM pg_enum WHERE enumlabel = 'PENDING' AND enumtypid = 'booking_status'::regtype) THEN
        ALTER TYPE booking_status ADD VALUE 'PENDING';
    END IF;
    
    -- Add CONFIRMED
    IF NOT EXISTS (SELECT 1 FROM pg_enum WHERE enumlabel = 'CONFIRMED' AND enumtypid = 'booking_status'::regtype) THEN
        ALTER TYPE booking_status ADD VALUE 'CONFIRMED';
    END IF;
    
    -- Add IN_PROGRESS
    IF NOT EXISTS (SELECT 1 FROM pg_enum WHERE enumlabel = 'IN_PROGRESS' AND enumtypid = 'booking_status'::regtype) THEN
        ALTER TYPE booking_status ADD VALUE 'IN_PROGRESS';
    END IF;
    
    -- Add EXPIRED
    IF NOT EXISTS (SELECT 1 FROM pg_enum WHERE enumlabel = 'EXPIRED' AND enumtypid = 'booking_status'::regtype) THEN
        ALTER TYPE booking_status ADD VALUE 'EXPIRED';
    END IF;
    
    -- Add REFUNDED
    IF NOT EXISTS (SELECT 1 FROM pg_enum WHERE enumlabel = 'REFUNDED' AND enumtypid = 'booking_status'::regtype) THEN
        ALTER TYPE booking_status ADD VALUE 'REFUNDED';
    END IF;
    
    -- Add NO_SHOW_STUDENT
    IF NOT EXISTS (SELECT 1 FROM pg_enum WHERE enumlabel = 'NO_SHOW_STUDENT' AND enumtypid = 'booking_status'::regtype) THEN
        ALTER TYPE booking_status ADD VALUE 'NO_SHOW_STUDENT';
    END IF;
    
    -- Add NO_SHOW_TEACHER
    IF NOT EXISTS (SELECT 1 FROM pg_enum WHERE enumlabel = 'NO_SHOW_TEACHER' AND enumtypid = 'booking_status'::regtype) THEN
        ALTER TYPE booking_status ADD VALUE 'NO_SHOW_TEACHER';
    END IF;
END $$;

-- Verify the enum values
SELECT enumlabel FROM pg_enum WHERE enumtypid = 'booking_status'::regtype ORDER BY enumsortorder;
