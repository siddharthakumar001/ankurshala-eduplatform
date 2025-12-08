-- V20__enhance_teacher_availability_system.sql
-- Enhanced Teacher Availability System with Weekly Schedules

-- Create teacher weekly availability table
CREATE TABLE teacher_weekly_availability (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    day_of_week INTEGER NOT NULL CHECK (day_of_week >= 0 AND day_of_week <= 6), -- 0=Sunday, 1=Monday, etc.
    start_time TIME NOT NULL,
    end_time TIME NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT true,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_teacher_weekly_availability UNIQUE (teacher_id, day_of_week, start_time, end_time)
);

-- Create teacher booking management table
CREATE TABLE teacher_booking_preferences (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    auto_accept_bookings BOOLEAN NOT NULL DEFAULT false,
    advance_booking_days INTEGER NOT NULL DEFAULT 7,
    minimum_session_duration INTEGER NOT NULL DEFAULT 30,
    maximum_session_duration INTEGER NOT NULL DEFAULT 120,
    cancellation_policy_hours INTEGER NOT NULL DEFAULT 24,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_teacher_booking_preferences UNIQUE (teacher_id)
);

-- Create teacher session feedback table
CREATE TABLE teacher_session_feedback (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    session_rating INTEGER CHECK (session_rating >= 1 AND session_rating <= 5),
    student_engagement TEXT,
    session_notes TEXT,
    improvement_suggestions TEXT,
    would_recommend BOOLEAN,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create teacher earnings table
CREATE TABLE teacher_earnings (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    session_date DATE NOT NULL,
    session_duration_minutes INTEGER NOT NULL,
    hourly_rate DECIMAL(8,2) NOT NULL,
    earnings_amount DECIMAL(8,2) NOT NULL,
    platform_fee DECIMAL(8,2) NOT NULL DEFAULT 0,
    net_earnings DECIMAL(8,2) NOT NULL,
    payment_status VARCHAR(20) NOT NULL DEFAULT 'PENDING' CHECK (payment_status IN ('PENDING', 'PROCESSED', 'PAID', 'FAILED')),
    payment_date TIMESTAMP,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Create teacher performance metrics table
CREATE TABLE teacher_performance_metrics (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
    metric_date DATE NOT NULL,
    total_sessions INTEGER NOT NULL DEFAULT 0,
    completed_sessions INTEGER NOT NULL DEFAULT 0,
    cancelled_sessions INTEGER NOT NULL DEFAULT 0,
    average_rating DECIMAL(3,2),
    total_earnings DECIMAL(10,2) NOT NULL DEFAULT 0,
    total_hours_taught DECIMAL(5,2) NOT NULL DEFAULT 0,
    student_satisfaction_score DECIMAL(3,2),
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uk_teacher_performance_metrics UNIQUE (teacher_id, metric_date)
);

-- Add indexes for better performance
CREATE INDEX idx_teacher_weekly_availability_teacher_id ON teacher_weekly_availability(teacher_id);
CREATE INDEX idx_teacher_weekly_availability_day ON teacher_weekly_availability(day_of_week);
CREATE INDEX idx_teacher_booking_preferences_teacher_id ON teacher_booking_preferences(teacher_id);
CREATE INDEX idx_teacher_session_feedback_teacher_id ON teacher_session_feedback(teacher_id);
CREATE INDEX idx_teacher_session_feedback_booking_id ON teacher_session_feedback(booking_id);
CREATE INDEX idx_teacher_earnings_teacher_id ON teacher_earnings(teacher_id);
CREATE INDEX idx_teacher_earnings_session_date ON teacher_earnings(session_date);
CREATE INDEX idx_teacher_performance_metrics_teacher_id ON teacher_performance_metrics(teacher_id);
CREATE INDEX idx_teacher_performance_metrics_date ON teacher_performance_metrics(metric_date);

-- Add some sample data for testing
INSERT INTO teacher_booking_preferences (teacher_id, auto_accept_bookings, advance_booking_days, minimum_session_duration, maximum_session_duration, cancellation_policy_hours)
SELECT id, false, 7, 30, 120, 24 FROM teachers WHERE id IN (SELECT teacher_id FROM teacher_profiles LIMIT 5);

-- Add sample weekly availability for existing teachers
INSERT INTO teacher_weekly_availability (teacher_id, day_of_week, start_time, end_time, is_available)
SELECT 
    t.id,
    generate_series(1, 5) as day_of_week, -- Monday to Friday
    '09:00:00'::time as start_time,
    '17:00:00'::time as end_time,
    true as is_available
FROM teachers t
WHERE t.id IN (SELECT teacher_id FROM teacher_profiles LIMIT 5);
