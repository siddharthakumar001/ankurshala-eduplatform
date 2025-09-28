-- Add board_id and subject_id columns to topics table
ALTER TABLE topics ADD COLUMN board_id BIGINT;
ALTER TABLE topics ADD COLUMN subject_id BIGINT;

-- Add foreign key constraints for topics.board_id -> boards.id
ALTER TABLE topics ADD CONSTRAINT fk_topics_board_id FOREIGN KEY (board_id) REFERENCES boards(id);

-- Add foreign key constraints for topics.subject_id -> subjects.id
ALTER TABLE topics ADD CONSTRAINT fk_topics_subject_id FOREIGN KEY (subject_id) REFERENCES subjects(id);

-- Create indexes on topics.board_id and topics.subject_id for better query performance
CREATE INDEX idx_topics_board_id ON topics(board_id);
CREATE INDEX idx_topics_subject_id ON topics(subject_id);
