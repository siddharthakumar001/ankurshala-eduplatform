-- Add soft delete columns to all content management tables
-- This migration adds soft_deleted columns for proper soft delete functionality

-- Add soft_deleted column to boards table (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'boards' AND column_name = 'soft_deleted') THEN
        ALTER TABLE boards ADD COLUMN soft_deleted BOOLEAN NOT NULL DEFAULT false;
    END IF;
END $$;

-- Add soft_deleted column to grades table (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'grades' AND column_name = 'soft_deleted') THEN
        ALTER TABLE grades ADD COLUMN soft_deleted BOOLEAN NOT NULL DEFAULT false;
    END IF;
END $$;

-- Add soft_deleted column to subjects table (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'subjects' AND column_name = 'soft_deleted') THEN
        ALTER TABLE subjects ADD COLUMN soft_deleted BOOLEAN NOT NULL DEFAULT false;
    END IF;
END $$;

-- Add soft_deleted column to chapters table (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'chapters' AND column_name = 'soft_deleted') THEN
        ALTER TABLE chapters ADD COLUMN soft_deleted BOOLEAN NOT NULL DEFAULT false;
    END IF;
END $$;

-- Add soft_deleted column to topics table (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'topics' AND column_name = 'soft_deleted') THEN
        ALTER TABLE topics ADD COLUMN soft_deleted BOOLEAN NOT NULL DEFAULT false;
    END IF;
END $$;

-- Add soft_deleted column to topic_notes table (if not exists)
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns WHERE table_name = 'topic_notes' AND column_name = 'soft_deleted') THEN
        ALTER TABLE topic_notes ADD COLUMN soft_deleted BOOLEAN NOT NULL DEFAULT false;
    END IF;
END $$;

-- Create indexes for soft_deleted columns for better query performance
CREATE INDEX IF NOT EXISTS idx_boards_soft_deleted ON boards(soft_deleted);
CREATE INDEX IF NOT EXISTS idx_grades_soft_deleted ON grades(soft_deleted);
CREATE INDEX IF NOT EXISTS idx_subjects_soft_deleted ON subjects(soft_deleted);
CREATE INDEX IF NOT EXISTS idx_chapters_soft_deleted ON chapters(soft_deleted);
CREATE INDEX IF NOT EXISTS idx_topics_soft_deleted ON topics(soft_deleted);
CREATE INDEX IF NOT EXISTS idx_topic_notes_soft_deleted ON topic_notes(soft_deleted);

-- Update existing unique constraints to exclude soft-deleted records
-- This ensures we can have duplicate names for soft-deleted records

-- Drop existing unique constraints
ALTER TABLE boards DROP CONSTRAINT IF EXISTS boards_name_key;
ALTER TABLE grades DROP CONSTRAINT IF EXISTS uk_grades_board_name;
ALTER TABLE subjects DROP CONSTRAINT IF EXISTS uk_subjects_grade_name;
ALTER TABLE chapters DROP CONSTRAINT IF EXISTS uk_chapters_subject_name;
ALTER TABLE topics DROP CONSTRAINT IF EXISTS uk_topics_chapter_title;

-- Create partial unique indexes that exclude soft-deleted records
CREATE UNIQUE INDEX uk_boards_name_active ON boards(name) WHERE soft_deleted = false;
CREATE UNIQUE INDEX uk_grades_board_name_active ON grades(board_id, name) WHERE soft_deleted = false;
CREATE UNIQUE INDEX uk_subjects_grade_name_active ON subjects(grade_id, name) WHERE soft_deleted = false;
CREATE UNIQUE INDEX uk_chapters_subject_name_active ON chapters(subject_id, name) WHERE soft_deleted = false;
CREATE UNIQUE INDEX uk_topics_chapter_title_active ON topics(chapter_id, title) WHERE soft_deleted = false;

-- Update topic code unique constraint to exclude soft-deleted records
DROP INDEX IF EXISTS uk_topics_code;
CREATE UNIQUE INDEX uk_topics_code_active ON topics(code) WHERE code IS NOT NULL AND soft_deleted = false;
