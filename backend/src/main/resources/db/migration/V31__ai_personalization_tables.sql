-- V31: AI-enabled personalization tables for adaptive learning
-- Creates tables for mastery tracking, quizzes, content chunks (RAG), and AI interaction logging

-- Enable pgvector extension for embeddings (requires PostgreSQL with pgvector installed)
CREATE EXTENSION IF NOT EXISTS vector;

-- ============================================================================
-- STUDENT TOPIC MASTERY - Tracks per-student, per-topic learning progress
-- ============================================================================
CREATE TABLE IF NOT EXISTS student_topic_mastery (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    topic_id BIGINT NOT NULL REFERENCES topics(id) ON DELETE CASCADE,
    
    -- Mastery metrics
    mastery_score DECIMAL(4,3) NOT NULL DEFAULT 0.300,  -- 0.000 to 1.000, default 0.3 for new topics
    confidence DECIMAL(4,3) NOT NULL DEFAULT 0.000,     -- Confidence in the mastery score
    
    -- Assessment tracking
    total_attempts INTEGER NOT NULL DEFAULT 0,
    correct_attempts INTEGER NOT NULL DEFAULT 0,
    last_assessed_at TIMESTAMP,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW(),
    
    -- Ensure one mastery record per student-topic pair
    CONSTRAINT uk_student_topic_mastery UNIQUE (student_id, topic_id)
);

CREATE INDEX idx_stm_student_id ON student_topic_mastery(student_id);
CREATE INDEX idx_stm_topic_id ON student_topic_mastery(topic_id);
CREATE INDEX idx_stm_mastery_score ON student_topic_mastery(mastery_score);
CREATE INDEX idx_stm_student_mastery ON student_topic_mastery(student_id, mastery_score);

-- ============================================================================
-- QUIZZES - Generated quiz instances
-- ============================================================================
CREATE TABLE IF NOT EXISTS quizzes (
    id BIGSERIAL PRIMARY KEY,
    
    -- Quiz metadata
    title VARCHAR(500) NOT NULL,
    description TEXT,
    
    -- Content linking
    topic_id BIGINT REFERENCES topics(id) ON DELETE SET NULL,
    subject_id BIGINT REFERENCES subjects(id) ON DELETE SET NULL,
    grade_id BIGINT REFERENCES grades(id) ON DELETE SET NULL,
    board_id BIGINT REFERENCES boards(id) ON DELETE SET NULL,
    
    -- Quiz configuration
    difficulty VARCHAR(50) NOT NULL DEFAULT 'MEDIUM',  -- EASY, MEDIUM, HARD, ADAPTIVE
    bloom_level VARCHAR(50),  -- REMEMBER, UNDERSTAND, APPLY, ANALYZE, EVALUATE, CREATE
    question_count INTEGER NOT NULL DEFAULT 5,
    time_limit_minutes INTEGER,
    
    -- Quiz type
    quiz_type VARCHAR(50) NOT NULL DEFAULT 'PRACTICE',  -- PRACTICE, ASSESSMENT, DIAGNOSTIC
    is_adaptive BOOLEAN NOT NULL DEFAULT FALSE,
    
    -- Language support
    language VARCHAR(20) NOT NULL DEFAULT 'en',  -- en, hi, etc.
    
    -- Generation metadata
    generated_by VARCHAR(100),  -- AI model identifier
    generation_prompt TEXT,
    
    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',  -- DRAFT, ACTIVE, ARCHIVED
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_quiz_topic_id ON quizzes(topic_id);
CREATE INDEX idx_quiz_subject_id ON quizzes(subject_id);
CREATE INDEX idx_quiz_status ON quizzes(status);
CREATE INDEX idx_quiz_difficulty ON quizzes(difficulty);

-- ============================================================================
-- QUIZ QUESTIONS - Individual questions within quizzes
-- ============================================================================
CREATE TABLE IF NOT EXISTS quiz_questions (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    
    -- Question content
    question_text TEXT NOT NULL,
    question_type VARCHAR(50) NOT NULL DEFAULT 'MCQ',  -- MCQ, SHORT_ANSWER, LONG_ANSWER, TRUE_FALSE, FILL_BLANK, ASSERTION_REASON
    
    -- Answer options (for MCQ, TRUE_FALSE, ASSERTION_REASON)
    options JSONB,  -- Array of {id, text, isCorrect}
    correct_answer TEXT,  -- For non-MCQ types
    
    -- Grading
    points INTEGER NOT NULL DEFAULT 1,
    rubric JSONB,  -- Grading rubric for subjective questions
    expected_answer_guide TEXT,  -- For AI grading of subjective answers
    
    -- Explanation and hints
    explanation TEXT,
    hint TEXT,
    
    -- Metadata
    bloom_level VARCHAR(50),  -- Per-question bloom level
    difficulty VARCHAR(50),
    topic_id BIGINT REFERENCES topics(id) ON DELETE SET NULL,
    
    -- Ordering
    sequence_number INTEGER NOT NULL DEFAULT 0,
    
    -- Language
    language VARCHAR(20) NOT NULL DEFAULT 'en',
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_qq_quiz_id ON quiz_questions(quiz_id);
CREATE INDEX idx_qq_topic_id ON quiz_questions(topic_id);
CREATE INDEX idx_qq_type ON quiz_questions(question_type);

-- ============================================================================
-- QUIZ ATTEMPTS - Student attempts at quizzes
-- ============================================================================
CREATE TABLE IF NOT EXISTS quiz_attempts (
    id BIGSERIAL PRIMARY KEY,
    quiz_id BIGINT NOT NULL REFERENCES quizzes(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    
    -- Attempt timing
    started_at TIMESTAMP NOT NULL DEFAULT NOW(),
    submitted_at TIMESTAMP,
    time_spent_seconds INTEGER,
    
    -- Scoring
    total_score DECIMAL(6,2),
    max_score DECIMAL(6,2),
    percentage DECIMAL(5,2),
    
    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'IN_PROGRESS',  -- IN_PROGRESS, SUBMITTED, GRADED, ABANDONED
    
    -- Grading metadata
    graded_at TIMESTAMP,
    graded_by VARCHAR(100),  -- 'AUTO', 'AI', or admin user ID
    
    -- AI feedback
    ai_feedback TEXT,
    strengths JSONB,  -- Array of strength areas
    weaknesses JSONB,  -- Array of weak areas for improvement
    
    -- Mastery impact
    mastery_delta DECIMAL(4,3),  -- Change in mastery score from this attempt
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_qa_quiz_id ON quiz_attempts(quiz_id);
CREATE INDEX idx_qa_student_id ON quiz_attempts(student_id);
CREATE INDEX idx_qa_status ON quiz_attempts(status);
CREATE INDEX idx_qa_student_quiz ON quiz_attempts(student_id, quiz_id);

-- ============================================================================
-- QUIZ ANSWERS - Individual answers within attempts
-- ============================================================================
CREATE TABLE IF NOT EXISTS quiz_answers (
    id BIGSERIAL PRIMARY KEY,
    attempt_id BIGINT NOT NULL REFERENCES quiz_attempts(id) ON DELETE CASCADE,
    question_id BIGINT NOT NULL REFERENCES quiz_questions(id) ON DELETE CASCADE,
    
    -- Student's answer
    answer_text TEXT,
    selected_option_ids JSONB,  -- For MCQ: array of selected option IDs
    
    -- Grading
    is_correct BOOLEAN,
    score DECIMAL(5,2),
    max_score DECIMAL(5,2),
    
    -- AI grading details
    grading_feedback TEXT,
    grading_rationale TEXT,  -- Why this score was given
    
    -- Timing
    time_spent_seconds INTEGER,
    answered_at TIMESTAMP,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_qans_attempt_id ON quiz_answers(attempt_id);
CREATE INDEX idx_qans_question_id ON quiz_answers(question_id);
CREATE INDEX idx_qans_is_correct ON quiz_answers(is_correct);

-- ============================================================================
-- CONTENT CHUNKS - RAG content store for AI retrieval
-- ============================================================================
CREATE TABLE IF NOT EXISTS content_chunks (
    id BIGSERIAL PRIMARY KEY,
    
    -- Content linking
    topic_id BIGINT REFERENCES topics(id) ON DELETE SET NULL,
    chapter_id BIGINT REFERENCES chapters(id) ON DELETE SET NULL,
    subject_id BIGINT REFERENCES subjects(id) ON DELETE SET NULL,
    
    -- Curriculum context
    board_id BIGINT REFERENCES boards(id) ON DELETE SET NULL,
    grade_id BIGINT REFERENCES grades(id) ON DELETE SET NULL,
    
    -- Source information
    source_type VARCHAR(100) NOT NULL,  -- TEXTBOOK, NCERT, NOTES, SYLLABUS, EXAMPLE, FORMULA, etc.
    source_ref VARCHAR(500),  -- Book name, page number, URL, etc.
    
    -- Content
    chunk_text TEXT NOT NULL,
    language VARCHAR(20) NOT NULL DEFAULT 'en',
    
    -- Vector embedding for semantic search (1536 dimensions for OpenAI ada-002)
    embedding vector(1536),
    
    -- Metadata for filtering and retrieval
    metadata JSONB,  -- Additional structured data: keywords, tags, difficulty, etc.
    
    -- Content quality
    is_verified BOOLEAN NOT NULL DEFAULT FALSE,
    verified_by BIGINT REFERENCES users(id) ON DELETE SET NULL,
    verified_at TIMESTAMP,
    
    -- Status
    status VARCHAR(50) NOT NULL DEFAULT 'ACTIVE',  -- ACTIVE, ARCHIVED, PENDING_REVIEW
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_cc_topic_id ON content_chunks(topic_id);
CREATE INDEX idx_cc_chapter_id ON content_chunks(chapter_id);
CREATE INDEX idx_cc_subject_id ON content_chunks(subject_id);
CREATE INDEX idx_cc_board_id ON content_chunks(board_id);
CREATE INDEX idx_cc_grade_id ON content_chunks(grade_id);
CREATE INDEX idx_cc_source_type ON content_chunks(source_type);
CREATE INDEX idx_cc_language ON content_chunks(language);
CREATE INDEX idx_cc_status ON content_chunks(status);

-- Vector similarity index using HNSW (faster for ANN search)
CREATE INDEX idx_cc_embedding ON content_chunks USING hnsw (embedding vector_cosine_ops);

-- ============================================================================
-- AI INTERACTIONS - Metadata logging for AI calls (privacy-preserving)
-- ============================================================================
CREATE TABLE IF NOT EXISTS ai_interactions (
    id BIGSERIAL PRIMARY KEY,
    
    -- User context (not logging actual content)
    student_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    session_id VARCHAR(100),  -- Browser/app session identifier
    
    -- Interaction type
    interaction_type VARCHAR(50) NOT NULL,  -- CHAT, QUIZ_GENERATE, QUIZ_GRADE, RECOMMENDATION, SUMMARY
    
    -- Request metadata (no raw user text)
    topic_id BIGINT REFERENCES topics(id) ON DELETE SET NULL,
    subject_id BIGINT REFERENCES subjects(id) ON DELETE SET NULL,
    
    -- AI provider info
    model_provider VARCHAR(50),  -- OPENAI, ANTHROPIC, GEMINI, etc.
    model_name VARCHAR(100),
    model_version VARCHAR(50),
    
    -- Performance metrics
    latency_ms INTEGER,
    prompt_tokens INTEGER,
    completion_tokens INTEGER,
    total_tokens INTEGER,
    
    -- RAG metrics
    retrieval_count INTEGER,  -- Number of chunks retrieved
    retrieval_score_avg DECIMAL(5,4),  -- Average similarity score
    
    -- Safety and moderation
    safety_flags JSONB,  -- Any safety filters triggered
    was_refused BOOLEAN NOT NULL DEFAULT FALSE,
    refusal_reason VARCHAR(500),
    
    -- Error tracking
    had_error BOOLEAN NOT NULL DEFAULT FALSE,
    error_type VARCHAR(100),
    error_message TEXT,
    
    -- Timestamps
    created_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_ai_student_id ON ai_interactions(student_id);
CREATE INDEX idx_ai_type ON ai_interactions(interaction_type);
CREATE INDEX idx_ai_created_at ON ai_interactions(created_at);
CREATE INDEX idx_ai_model ON ai_interactions(model_provider, model_name);
CREATE INDEX idx_ai_had_error ON ai_interactions(had_error);
CREATE INDEX idx_ai_was_refused ON ai_interactions(was_refused);

-- ============================================================================
-- TRIGGERS for updated_at timestamps
-- ============================================================================
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ LANGUAGE plpgsql;

CREATE TRIGGER trg_student_topic_mastery_updated_at
    BEFORE UPDATE ON student_topic_mastery
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_quizzes_updated_at
    BEFORE UPDATE ON quizzes
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_quiz_attempts_updated_at
    BEFORE UPDATE ON quiz_attempts
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trg_content_chunks_updated_at
    BEFORE UPDATE ON content_chunks
    FOR EACH ROW
    EXECUTE FUNCTION update_updated_at_column();

-- ============================================================================
-- COMMENTS for documentation
-- ============================================================================
COMMENT ON TABLE student_topic_mastery IS 'Tracks per-student, per-topic mastery scores for adaptive learning';
COMMENT ON COLUMN student_topic_mastery.mastery_score IS 'Mastery score from 0.0 to 1.0, default 0.3 for unassessed topics';
COMMENT ON COLUMN student_topic_mastery.confidence IS 'Confidence in mastery score based on number of assessments';

COMMENT ON TABLE quizzes IS 'Generated quiz instances with configurable difficulty and bloom levels';
COMMENT ON TABLE quiz_questions IS 'Individual questions within quizzes, supporting multiple question types';
COMMENT ON TABLE quiz_attempts IS 'Student attempts at quizzes with scoring and AI feedback';
COMMENT ON TABLE quiz_answers IS 'Individual answers within quiz attempts';

COMMENT ON TABLE content_chunks IS 'RAG content store with vector embeddings for semantic search';
COMMENT ON COLUMN content_chunks.embedding IS 'Vector embedding (1536 dims) for semantic similarity search';

COMMENT ON TABLE ai_interactions IS 'Privacy-preserving AI interaction logging for observability';

