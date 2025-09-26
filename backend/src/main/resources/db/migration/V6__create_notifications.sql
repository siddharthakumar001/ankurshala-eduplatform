-- Notifications Migration
-- Create notifications table for in-app and email notifications

CREATE TYPE notification_audience AS ENUM ('STUDENT', 'TEACHER', 'BOTH');
CREATE TYPE notification_delivery AS ENUM ('IN_APP', 'EMAIL', 'BOTH');
CREATE TYPE notification_status AS ENUM ('QUEUED', 'SENT', 'FAILED');

CREATE TABLE IF NOT EXISTS notifications (
    id BIGSERIAL PRIMARY KEY,
    user_id BIGINT REFERENCES users(id),
    title VARCHAR(255) NOT NULL,
    body TEXT NOT NULL,
    audience notification_audience NOT NULL,
    delivery notification_delivery NOT NULL,
    status notification_status DEFAULT 'QUEUED',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    sent_at TIMESTAMP NULL
);

-- Create indexes for efficient lookups
CREATE INDEX idx_notifications_user_id ON notifications(user_id);
CREATE INDEX idx_notifications_audience ON notifications(audience);
CREATE INDEX idx_notifications_status ON notifications(status);
CREATE INDEX idx_notifications_created_at ON notifications(created_at);
