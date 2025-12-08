-- Fix booking_bookmarks and booking_notes tables to match entity requirements
-- This adds missing columns that entities expect but weren't in V14

-- Add missing columns to booking_bookmarks
ALTER TABLE booking_bookmarks 
ADD COLUMN IF NOT EXISTS ts_seconds INTEGER,
ADD COLUMN IF NOT EXISTS note TEXT;

-- Remove student_id if it exists (not in entity)
ALTER TABLE booking_bookmarks 
DROP CONSTRAINT IF EXISTS booking_bookmarks_student_id_fkey,
DROP CONSTRAINT IF EXISTS uk_booking_bookmark,
DROP COLUMN IF EXISTS student_id;

-- Add missing columns to booking_notes
ALTER TABLE booking_notes
ADD COLUMN IF NOT EXISTS author_role VARCHAR(10),
ADD COLUMN IF NOT EXISTS url VARCHAR(500),
ADD COLUMN IF NOT EXISTS text TEXT;

-- Set default for existing rows
UPDATE booking_notes SET author_role = 'STUDENT' WHERE author_role IS NULL;
UPDATE booking_notes SET url = '' WHERE url IS NULL AND author_role IS NOT NULL;
UPDATE booking_notes SET text = content WHERE text IS NULL AND content IS NOT NULL;

