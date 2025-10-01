-- Add soft delete columns and other missing fields for content management
-- This migration ensures all entities have proper soft delete support and denormalized foreign keys

-- Add soft_deleted and deleted_at to boards
ALTER TABLE boards ADD COLUMN IF NOT EXISTS soft_deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE boards ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;

-- Add board_id and soft_deleted to grades
-- board_id might already exist from V13/V15, so we check first
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.columns 
        WHERE table_name = 'grades' AND column_name = 'board_id'
    ) THEN
        ALTER TABLE grades ADD COLUMN board_id BIGINT NOT NULL DEFAULT 1;
    END IF;
END $$;

ALTER TABLE grades ADD COLUMN IF NOT EXISTS soft_deleted BOOLEAN NOT NULL DEFAULT false;

-- Add foreign key constraint for grades -> boards if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_grades_board' AND table_name = 'grades'
    ) THEN
        ALTER TABLE grades ADD CONSTRAINT fk_grades_board FOREIGN KEY (board_id) REFERENCES boards(id);
    END IF;
END $$;

-- Add missing fields to subjects
-- board_id might already exist from V10, grade_id from V16
ALTER TABLE subjects ADD COLUMN IF NOT EXISTS soft_deleted BOOLEAN NOT NULL DEFAULT false;

-- Ensure proper foreign key exists for subjects -> grades
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_subjects_grade' AND table_name = 'subjects'
    ) THEN
        ALTER TABLE subjects ADD CONSTRAINT fk_subjects_grade FOREIGN KEY (grade_id) REFERENCES grades(id);
    END IF;
END $$;

-- Add grade_id to chapters if not exists and soft_deleted
ALTER TABLE chapters ADD COLUMN IF NOT EXISTS grade_id BIGINT;
ALTER TABLE chapters ADD COLUMN IF NOT EXISTS soft_deleted BOOLEAN NOT NULL DEFAULT false;

-- Update chapters to inherit grade_id from their subject
UPDATE chapters 
SET grade_id = s.grade_id 
FROM subjects s 
WHERE chapters.subject_id = s.id AND chapters.grade_id IS NULL;

ALTER TABLE chapters ALTER COLUMN grade_id SET NOT NULL;

-- Add foreign key constraint for chapters -> grades
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_chapters_grade' AND table_name = 'chapters'
    ) THEN
        ALTER TABLE chapters ADD CONSTRAINT fk_chapters_grade FOREIGN KEY (grade_id) REFERENCES grades(id);
    END IF;
END $$;

-- Add grade_id to topics and soft_deleted
-- board_id and subject_id might already exist from V11
ALTER TABLE topics ADD COLUMN IF NOT EXISTS grade_id BIGINT;
ALTER TABLE topics ADD COLUMN IF NOT EXISTS soft_deleted BOOLEAN NOT NULL DEFAULT false;

-- Update topics to inherit grade_id from their chapter
UPDATE topics 
SET grade_id = c.grade_id 
FROM chapters c 
WHERE topics.chapter_id = c.id AND topics.grade_id IS NULL;

ALTER TABLE topics ALTER COLUMN grade_id SET NOT NULL;

-- Add foreign key constraint for topics -> grades
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_topics_grade' AND table_name = 'topics'
    ) THEN
        ALTER TABLE topics ADD CONSTRAINT fk_topics_grade FOREIGN KEY (grade_id) REFERENCES grades(id);
    END IF;
END $$;

-- Add foreign key constraints for topics -> subjects if not exists
DO $$
BEGIN
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'fk_topics_subject' AND table_name = 'topics'
    ) THEN
        ALTER TABLE topics ADD CONSTRAINT fk_topics_subject FOREIGN KEY (subject_id) REFERENCES subjects(id);
    END IF;
END $$;

-- Add soft_deleted and hierarchy fields to topic_notes
ALTER TABLE topic_notes ADD COLUMN IF NOT EXISTS soft_deleted BOOLEAN NOT NULL DEFAULT false;
ALTER TABLE topic_notes ADD COLUMN IF NOT EXISTS deleted_at TIMESTAMP;
ALTER TABLE topic_notes ADD COLUMN IF NOT EXISTS board_id BIGINT;
ALTER TABLE topic_notes ADD COLUMN IF NOT EXISTS grade_id BIGINT;
ALTER TABLE topic_notes ADD COLUMN IF NOT EXISTS subject_id BIGINT;
ALTER TABLE topic_notes ADD COLUMN IF NOT EXISTS chapter_id BIGINT;

-- Update topic_notes to inherit hierarchy from their topics
UPDATE topic_notes 
SET board_id = t.board_id, 
    grade_id = t.grade_id,
    subject_id = t.subject_id,
    chapter_id = t.chapter_id
FROM topics t 
WHERE topic_notes.topic_id = t.id AND topic_notes.board_id IS NULL;

-- Create indexes for better performance
CREATE INDEX IF NOT EXISTS idx_boards_active_soft_deleted ON boards(active, soft_deleted);
CREATE INDEX IF NOT EXISTS idx_grades_board_active_soft_deleted ON grades(board_id, active, soft_deleted);
CREATE INDEX IF NOT EXISTS idx_subjects_board_grade_active_soft_deleted ON subjects(board_id, grade_id, active, soft_deleted);
CREATE INDEX IF NOT EXISTS idx_chapters_board_grade_subject_active_soft_deleted ON chapters(board_id, grade_id, subject_id, active, soft_deleted);
CREATE INDEX IF NOT EXISTS idx_topics_board_grade_subject_chapter_active_soft_deleted ON topics(board_id, grade_id, subject_id, chapter_id, active, soft_deleted);
CREATE INDEX IF NOT EXISTS idx_topic_notes_topic_active_soft_deleted ON topic_notes(topic_id, active, soft_deleted);

-- Add search indexes for name/title fields
CREATE INDEX IF NOT EXISTS idx_boards_name_search ON boards USING gin(to_tsvector('english', name));
CREATE INDEX IF NOT EXISTS idx_grades_name_search ON grades USING gin(to_tsvector('english', name));
CREATE INDEX IF NOT EXISTS idx_subjects_name_search ON subjects USING gin(to_tsvector('english', name));
CREATE INDEX IF NOT EXISTS idx_chapters_name_search ON chapters USING gin(to_tsvector('english', name));
CREATE INDEX IF NOT EXISTS idx_topics_title_search ON topics USING gin(to_tsvector('english', title));
CREATE INDEX IF NOT EXISTS idx_topic_notes_title_search ON topic_notes USING gin(to_tsvector('english', title));

-- Ensure topic code uniqueness constraint is scoped per chapter
DO $$
BEGIN
    -- Drop existing constraint if it exists globally
    IF EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'topics_code_key' AND table_name = 'topics'
    ) THEN
        ALTER TABLE topics DROP CONSTRAINT topics_code_key;
    END IF;
    
    -- Add constraint scoped to chapter
    IF NOT EXISTS (
        SELECT 1 FROM information_schema.table_constraints 
        WHERE constraint_name = 'uk_topics_chapter_code' AND table_name = 'topics'
    ) THEN
        ALTER TABLE topics ADD CONSTRAINT uk_topics_chapter_code UNIQUE (chapter_id, code);
    END IF;
END $$;
