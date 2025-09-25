-- Content Taxonomy Tables Migration
-- This migration creates the hierarchical content structure: Board -> Grade -> Subject -> Chapter -> Topic

-- Boards table
CREATE TABLE boards (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Grades table
CREATE TABLE grades (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE,
    display_name VARCHAR(100) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Subjects table
CREATE TABLE subjects (
    id BIGSERIAL PRIMARY KEY,
    name VARCHAR(100) NOT NULL UNIQUE,
    active BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Chapters table (belongs to Subject)
CREATE TABLE chapters (
    id BIGSERIAL PRIMARY KEY,
    subject_id BIGINT NOT NULL,
    name VARCHAR(200) NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_chapters_subject FOREIGN KEY (subject_id) REFERENCES subjects(id),
    CONSTRAINT uk_chapters_subject_name UNIQUE (subject_id, name)
);

-- Topics table (belongs to Chapter)
CREATE TABLE topics (
    id BIGSERIAL PRIMARY KEY,
    chapter_id BIGINT NOT NULL,
    title VARCHAR(300) NOT NULL,
    code VARCHAR(100) NULL,
    description TEXT,
    summary TEXT,
    expected_time_mins INTEGER,
    active BOOLEAN NOT NULL DEFAULT true,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_topics_chapter FOREIGN KEY (chapter_id) REFERENCES chapters(id),
    CONSTRAINT uk_topics_chapter_title UNIQUE (chapter_id, title)
);

-- Create partial unique constraint for topic code (only when not null)
CREATE UNIQUE INDEX uk_topics_code ON topics(code) WHERE code IS NOT NULL;

-- Topic Links table (for prerequisites and related topics)
CREATE TABLE topic_links (
    id BIGSERIAL PRIMARY KEY,
    topic_id BIGINT NOT NULL,
    type VARCHAR(20) NOT NULL CHECK (type IN ('PREREQUISITE', 'RELATED')),
    linked_topic_id BIGINT NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_topic_links_topic FOREIGN KEY (topic_id) REFERENCES topics(id),
    CONSTRAINT fk_topic_links_linked_topic FOREIGN KEY (linked_topic_id) REFERENCES topics(id),
    CONSTRAINT uk_topic_links_unique UNIQUE (topic_id, type, linked_topic_id)
);

-- Topic Notes table (belongs to Topic)
CREATE TABLE topic_notes (
    id BIGSERIAL PRIMARY KEY,
    topic_id BIGINT NOT NULL,
    title VARCHAR(300) NOT NULL,
    content TEXT NOT NULL,
    active BOOLEAN NOT NULL DEFAULT true,
    attachments JSONB NULL,
    deleted_at TIMESTAMP NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_topic_notes_topic FOREIGN KEY (topic_id) REFERENCES topics(id)
);

-- Update import_jobs table to support new content import structure
ALTER TABLE import_jobs 
ADD COLUMN type VARCHAR(50) DEFAULT 'CONTENT_CSV',
ADD COLUMN stats JSONB,
ADD COLUMN errors JSONB;

-- Rename existing columns to match new naming convention
ALTER TABLE import_jobs 
RENAME COLUMN total_records TO total_rows;

ALTER TABLE import_jobs 
RENAME COLUMN successful_records TO success_rows;

ALTER TABLE import_jobs 
RENAME COLUMN failed_records TO error_rows;

-- Remove unused processed_records column
ALTER TABLE import_jobs 
DROP COLUMN processed_records;

-- Update existing status values to match new enum
UPDATE import_jobs SET status = 'SUCCEEDED' WHERE status = 'COMPLETED';
UPDATE import_jobs SET status = 'RUNNING' WHERE status = 'IN_PROGRESS';

-- Update status enum to include new values
ALTER TABLE import_jobs 
DROP CONSTRAINT IF EXISTS chk_import_jobs_status;

ALTER TABLE import_jobs 
ADD CONSTRAINT chk_import_jobs_status 
CHECK (status IN ('PENDING', 'RUNNING', 'SUCCEEDED', 'FAILED', 'PARTIALLY_SUCCEEDED'));

-- Create indexes for better performance
CREATE INDEX idx_boards_name ON boards(name);
CREATE INDEX idx_boards_active ON boards(active);
CREATE INDEX idx_grades_name ON grades(name);
CREATE INDEX idx_grades_active ON grades(active);
CREATE INDEX idx_subjects_name ON subjects(name);
CREATE INDEX idx_subjects_active ON subjects(active);
CREATE INDEX idx_chapters_subject_id ON chapters(subject_id);
CREATE INDEX idx_chapters_name ON chapters(name);
CREATE INDEX idx_chapters_active ON chapters(active);
CREATE INDEX idx_chapters_deleted_at ON chapters(deleted_at);
CREATE INDEX idx_topics_chapter_id ON topics(chapter_id);
CREATE INDEX idx_topics_title ON topics(title);
CREATE INDEX idx_topics_code ON topics(code) WHERE code IS NOT NULL;
CREATE INDEX idx_topics_active ON topics(active);
CREATE INDEX idx_topics_deleted_at ON topics(deleted_at);
CREATE INDEX idx_topic_links_topic_id ON topic_links(topic_id);
CREATE INDEX idx_topic_links_linked_topic_id ON topic_links(linked_topic_id);
CREATE INDEX idx_topic_notes_topic_id ON topic_notes(topic_id);
CREATE INDEX idx_topic_notes_active ON topic_notes(active);
CREATE INDEX idx_topic_notes_deleted_at ON topic_notes(deleted_at);

-- Insert default data
INSERT INTO boards (name) VALUES ('CBSE'), ('ICSE'), ('State Board') ON CONFLICT (name) DO NOTHING;

INSERT INTO grades (name, display_name) VALUES 
('1', 'Grade 1'),
('2', 'Grade 2'),
('3', 'Grade 3'),
('4', 'Grade 4'),
('5', 'Grade 5'),
('6', 'Grade 6'),
('7', 'Grade 7'),
('8', 'Grade 8'),
('9', 'Grade 9'),
('10', 'Grade 10'),
('11', 'Grade 11'),
('12', 'Grade 12')
ON CONFLICT (name) DO NOTHING;

INSERT INTO subjects (name) VALUES 
('Mathematics'),
('Physics'),
('Chemistry'),
('Biology'),
('English'),
('Hindi'),
('Social Science'),
('Computer Science'),
('Economics'),
('Business Studies'),
('Accountancy')
ON CONFLICT (name) DO NOTHING;
