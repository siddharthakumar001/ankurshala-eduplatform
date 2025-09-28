-- Add board_id column to subjects table
ALTER TABLE subjects ADD COLUMN board_id BIGINT;

-- Add board_id column to chapters table  
ALTER TABLE chapters ADD COLUMN board_id BIGINT;

-- Add foreign key constraint for subjects.board_id -> boards.id
ALTER TABLE subjects ADD CONSTRAINT fk_subjects_board_id FOREIGN KEY (board_id) REFERENCES boards(id);

-- Add foreign key constraint for chapters.board_id -> boards.id
ALTER TABLE chapters ADD CONSTRAINT fk_chapters_board_id FOREIGN KEY (board_id) REFERENCES boards(id);

-- Add unique constraint for subjects (board_id, name) to ensure subject names are unique per board
ALTER TABLE subjects ADD CONSTRAINT uk_subjects_board_name UNIQUE (board_id, name);

-- Create index on subjects.board_id for better query performance
CREATE INDEX idx_subjects_board_id ON subjects(board_id);

-- Create index on chapters.board_id for better query performance
CREATE INDEX idx_chapters_board_id ON chapters(board_id);
