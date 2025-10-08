-- Notifications Migration
-- Create notifications table for in-app and email notifications

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    audience VARCHAR(20) NOT NULL CHECK (audience IN ('STUDENT', 'TEACHER', 'BOTH')),
    delivery VARCHAR(20) NOT NULL CHECK (delivery IN ('IN_APP', 'EMAIL', 'BOTH')),
    status VARCHAR(20) DEFAULT 'QUEUED' CHECK (status IN ('QUEUED', 'SENT', 'FAILED')),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL
);

-- Create indexes for efficient lookups
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_audience ON notifications(audience);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
