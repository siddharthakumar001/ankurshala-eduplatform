-- Change attachments column from JSONB to TEXT to avoid conversion issues
-- This migration changes the topic_notes.attachments column from JSONB to TEXT

ALTER TABLE topic_notes 
ALTER COLUMN attachments TYPE TEXT USING attachments::TEXT;
