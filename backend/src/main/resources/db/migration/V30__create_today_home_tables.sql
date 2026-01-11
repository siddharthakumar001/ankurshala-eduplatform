-- ============================================================================
-- Today Home Feature Migration
-- Version: 1.0.0
-- Date: January 10, 2026
-- 
-- Purpose: Add tables and indexes for "Today Home" personalized daily plan
--          with caching, practice tracking, and weak topic recommendations
-- ============================================================================

BEGIN;

-- Step 1: Create daily plan progress tracking table
-- Tracks which steps a student completed on their daily plan
CREATE TABLE IF NOT EXISTS daily_plan_progress (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    plan_date DATE NOT NULL,
    step_type VARCHAR(50) NOT NULL, -- PRACTICE, REVISE_NOTE, FOCUS_SPRINT, BOOKING_COMPANION
    step_identifier VARCHAR(255), -- topicId, noteId, bookingId, etc.
    completed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completion_metadata JSONB, -- Additional data about completion
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure student can only complete same step once per day
    UNIQUE(student_id, plan_date, step_type, step_identifier)
);

-- Step 2: Create indexes for fast queries
CREATE INDEX idx_daily_plan_progress_student_date 
ON daily_plan_progress(student_id, plan_date DESC);

CREATE INDEX idx_daily_plan_progress_student_recent 
ON daily_plan_progress(student_id, completed_at DESC);

-- Step 3: Create weak topic recommendations table
-- Stores system-generated weak topic recommendations with reasoning
CREATE TABLE IF NOT EXISTS weak_topic_recommendations (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic_id BIGINT NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    recommendation_reason TEXT NOT NULL, -- Why this topic is recommended
    prerequisite_gaps TEXT, -- Missing prerequisite topics
    confidence_score DECIMAL(3,2) NOT NULL DEFAULT 0.00, -- 0.00 to 1.00
    last_practice_date TIMESTAMP,
    practice_attempts_count INT NOT NULL DEFAULT 0,
    avg_score DECIMAL(5,2), -- Average performance on this topic
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    expires_at TIMESTAMP, -- Recommendation expiry
    is_active BOOLEAN NOT NULL DEFAULT TRUE,
    
    -- One active recommendation per topic per student
    UNIQUE(student_id, topic_id, is_active)
);

CREATE INDEX idx_weak_topic_recommendations_student_active 
ON weak_topic_recommendations(student_id, is_active, confidence_score DESC)
WHERE is_active = TRUE;

CREATE INDEX idx_weak_topic_recommendations_expires 
ON weak_topic_recommendations(expires_at)
WHERE is_active = TRUE AND expires_at IS NOT NULL;

-- Step 4: Create practice queue table for daily practice items
CREATE TABLE IF NOT EXISTS daily_practice_queue (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic_id BIGINT NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    queue_date DATE NOT NULL,
    priority INT NOT NULL DEFAULT 5, -- 1 (highest) to 10 (lowest)
    source VARCHAR(50) NOT NULL, -- SPACED_REPETITION, WEAK_TOPIC, ADAPTIVE, MANUAL
    due_for_review BOOLEAN NOT NULL DEFAULT FALSE,
    
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at TIMESTAMP,
    
    -- Prevent duplicate topic in same day's queue
    UNIQUE(student_id, topic_id, queue_date)
);

CREATE INDEX idx_daily_practice_queue_student_date 
ON daily_practice_queue(student_id, queue_date, priority ASC)
WHERE completed_at IS NULL;

CREATE INDEX idx_daily_practice_queue_due_review 
ON daily_practice_queue(student_id, due_for_review, queue_date)
WHERE completed_at IS NULL AND due_for_review = TRUE;

-- Step 5: Add trigger to update updated_at timestamps
CREATE OR REPLACE FUNCTION update_today_home_updated_at()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = CURRENT_TIMESTAMP;
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER update_daily_plan_progress_updated_at
    BEFORE UPDATE ON daily_plan_progress
    FOR EACH ROW
    EXECUTE FUNCTION update_today_home_updated_at();

CREATE TRIGGER update_weak_topic_recommendations_updated_at
    BEFORE UPDATE ON weak_topic_recommendations
    FOR EACH ROW
    EXECUTE FUNCTION update_today_home_updated_at();

COMMIT;

-- Performance notes:
-- 1. daily_plan_progress: Expected ~100 rows per student per month
-- 2. weak_topic_recommendations: Expected ~5-10 active recommendations per student
-- 3. daily_practice_queue: Expected ~3-10 items per student per day
-- 4. All indexes support fast reads for daily plan generation (<10ms target)
