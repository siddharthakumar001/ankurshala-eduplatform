-- Create student_study_list table
CREATE TABLE student_study_list (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL,
    topic_id BIGINT NOT NULL,
    status VARCHAR(20) NOT NULL CHECK (status IN ('ADDED', 'IN_PROGRESS', 'DONE')),
    added_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    done_at TIMESTAMP WITH TIME ZONE,
    notes VARCHAR(1000),
    CONSTRAINT uk_student_topic UNIQUE(student_id, topic_id),
    CONSTRAINT fk_study_list_student FOREIGN KEY (student_id) REFERENCES student_profiles(id) ON DELETE CASCADE,
    CONSTRAINT fk_study_list_topic FOREIGN KEY (topic_id) REFERENCES topics(id) ON DELETE CASCADE
);

-- Create indexes for performance
CREATE INDEX idx_study_list_student ON student_study_list(student_id);
CREATE INDEX idx_study_list_topic ON student_study_list(topic_id);
CREATE INDEX idx_study_list_status ON student_study_list(status);
CREATE INDEX idx_study_list_added_at ON student_study_list(added_at DESC);

-- Add comment
COMMENT ON TABLE student_study_list IS 'Tracks topics added to student study list with completion status';
