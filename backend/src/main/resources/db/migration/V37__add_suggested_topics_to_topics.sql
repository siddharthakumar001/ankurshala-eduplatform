-- Migration: Add suggested_topics field to topics table for AI integration
-- This field will store AI-generated or manually curated suggested related topics
-- as a comma-separated or JSON format for quick access without complex joins

-- Add suggested_topics column to topics table
ALTER TABLE topics ADD COLUMN IF NOT EXISTS suggested_topics TEXT;

-- Add comment to document the field purpose
COMMENT ON COLUMN topics.suggested_topics IS 'AI-generated or curated suggested related topics for enhanced learning recommendations';

-- Create index for suggested_topics for better search performance (if content is searchable)
CREATE INDEX IF NOT EXISTS idx_topics_suggested_topics_gin ON topics USING gin(to_tsvector('english', COALESCE(suggested_topics, '')));
