-- Pricing Rules Migration
-- Create pricing_rules table for taxonomy-based pricing

CREATE TABLE IF NOT EXISTS pricing_rules (
    id BIGSERIAL PRIMARY KEY,
    board_id BIGINT REFERENCES boards(id),
    grade_id BIGINT REFERENCES grades(id),
    subject_id BIGINT REFERENCES subjects(id),
    chapter_id BIGINT REFERENCES chapters(id),
    topic_id BIGINT REFERENCES topics(id),
    hourly_rate DECIMAL(8,2) NOT NULL,
    active BOOLEAN DEFAULT TRUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    
    -- Ensure at least one taxonomy FK is provided
    CONSTRAINT chk_pricing_rule_scope CHECK (
        board_id IS NOT NULL OR 
        grade_id IS NOT NULL OR 
        subject_id IS NOT NULL OR 
        chapter_id IS NOT NULL OR 
        topic_id IS NOT NULL
    )
);

-- Create indexes for efficient lookups
CREATE INDEX idx_pricing_rules_board_id ON pricing_rules(board_id);
CREATE INDEX idx_pricing_rules_grade_id ON pricing_rules(grade_id);
CREATE INDEX idx_pricing_rules_subject_id ON pricing_rules(subject_id);
CREATE INDEX idx_pricing_rules_chapter_id ON pricing_rules(chapter_id);
CREATE INDEX idx_pricing_rules_topic_id ON pricing_rules(topic_id);
CREATE INDEX idx_pricing_rules_active ON pricing_rules(active);

-- Create unique constraint to prevent overlapping identical scope
CREATE UNIQUE INDEX idx_pricing_rules_unique_scope ON pricing_rules(
    COALESCE(board_id, 0),
    COALESCE(grade_id, 0),
    COALESCE(subject_id, 0),
    COALESCE(chapter_id, 0),
    COALESCE(topic_id, 0)
) WHERE active = TRUE;
