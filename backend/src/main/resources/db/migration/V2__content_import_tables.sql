-- Create import_jobs table first (referenced by course_content)
CREATE TABLE import_jobs (
    id BIGSERIAL PRIMARY KEY,
    file_name VARCHAR(255) NOT NULL,
    file_type VARCHAR(50) NOT NULL,
    file_size BIGINT,
    status VARCHAR(50) NOT NULL DEFAULT 'PENDING',
    total_records INTEGER DEFAULT 0,
    processed_records INTEGER DEFAULT 0,
    successful_records INTEGER DEFAULT 0,
    failed_records INTEGER DEFAULT 0,
    error_message TEXT,
    started_at TIMESTAMP,
    completed_at TIMESTAMP,
    created_by_user_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_import_jobs_created_by FOREIGN KEY (created_by_user_id) REFERENCES users(id)
);

-- Create course_content table (references import_jobs)
CREATE TABLE course_content (
    id BIGSERIAL PRIMARY KEY,
    class_level VARCHAR(50) NOT NULL,
    subject VARCHAR(255) NOT NULL,
    chapter VARCHAR(255),
    topic VARCHAR(300),
    brief_description TEXT,
    summary TEXT,
    suggested_topics TEXT,
    resource_url VARCHAR(500),
    expected_time_minutes INTEGER,
    educational_board VARCHAR(50),
    is_active BOOLEAN NOT NULL DEFAULT true,
    import_job_id BIGINT,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_course_content_import_job FOREIGN KEY (import_job_id) REFERENCES import_jobs(id)
);

-- Create indexes for better performance
CREATE INDEX idx_course_content_class_level ON course_content(class_level);
CREATE INDEX idx_course_content_subject ON course_content(subject);
CREATE INDEX idx_import_jobs_status ON import_jobs(status);
CREATE INDEX idx_import_jobs_created_at ON import_jobs(created_at);
