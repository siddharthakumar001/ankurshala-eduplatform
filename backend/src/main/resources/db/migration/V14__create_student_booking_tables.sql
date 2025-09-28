-- Student Booking Tables Migration
-- This migration creates the booking system for student-teacher sessions

-- Booking status enum
CREATE TYPE booking_status AS ENUM (
    'REQUESTED',    -- Student has requested a booking
    'ACCEPTED',     -- Teacher has accepted the booking
    'RESCHEDULED',  -- Booking has been rescheduled
    'CANCELLED',    -- Booking has been cancelled
    'COMPLETED',    -- Session has been completed
    'NO_SHOW'       -- Student didn't show up
);

-- Bookings table
CREATE TABLE bookings (
    id BIGSERIAL PRIMARY KEY,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    teacher_id BIGINT REFERENCES users(id) ON DELETE SET NULL,
    topic_id BIGINT NOT NULL REFERENCES topics(id) ON DELETE RESTRICT,
    
    -- Session details
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_minutes INTEGER NOT NULL CHECK (duration_minutes >= 30 AND duration_minutes <= 120),
    
    -- Status and workflow
    status booking_status NOT NULL DEFAULT 'REQUESTED',
    acceptance_token VARCHAR(255) UNIQUE, -- For teacher acceptance
    accepted_at TIMESTAMP WITH TIME ZONE,
    cancelled_at TIMESTAMP WITH TIME ZONE,
    cancellation_reason TEXT,
    
    -- Pricing snapshot (at time of booking)
    price_min DECIMAL(8,2) NOT NULL,
    price_max DECIMAL(8,2) NOT NULL,
    price_currency VARCHAR(3) NOT NULL DEFAULT 'INR',
    pricing_rule_id BIGINT REFERENCES pricing_rules(id),
    
    -- Fees and waivers
    cancellation_fee DECIMAL(8,2) DEFAULT 0,
    reschedule_fee DECIMAL(8,2) DEFAULT 0,
    fee_waiver_id BIGINT REFERENCES fee_waivers(id),
    
    -- Session notes and feedback
    student_notes TEXT,
    teacher_notes TEXT,
    student_feedback TEXT,
    teacher_feedback TEXT,
    rating INTEGER CHECK (rating >= 1 AND rating <= 5),
    
    -- Metadata
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    -- Constraints
    CONSTRAINT chk_booking_times CHECK (end_time > start_time),
    CONSTRAINT chk_booking_duration CHECK (duration_minutes = EXTRACT(EPOCH FROM (end_time - start_time))/60)
);

-- Booking bookmarks table
CREATE TABLE booking_bookmarks (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    student_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT uk_booking_bookmark UNIQUE (booking_id, student_id)
);

-- Booking notes table (for additional notes during/after session)
CREATE TABLE booking_notes (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NOT NULL REFERENCES bookings(id) ON DELETE CASCADE,
    author_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    content TEXT NOT NULL,
    note_type VARCHAR(20) NOT NULL DEFAULT 'GENERAL' CHECK (note_type IN ('GENERAL', 'FEEDBACK', 'REMINDER')),
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW()
);

-- Teacher availability slots table (for specific time slots)
CREATE TABLE teacher_availability_slots (
    id BIGSERIAL PRIMARY KEY,
    teacher_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    start_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_time TIMESTAMP WITH TIME ZONE NOT NULL,
    is_available BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    updated_at TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT NOW(),
    
    CONSTRAINT chk_slot_times CHECK (end_time > start_time)
);

-- Create indexes for performance
CREATE INDEX idx_bookings_student_id ON bookings(student_id);
CREATE INDEX idx_bookings_teacher_id ON bookings(teacher_id);
CREATE INDEX idx_bookings_topic_id ON bookings(topic_id);
CREATE INDEX idx_bookings_status ON bookings(status);
CREATE INDEX idx_bookings_start_time ON bookings(start_time);
CREATE INDEX idx_bookings_end_time ON bookings(end_time);
CREATE INDEX idx_bookings_created_at ON bookings(created_at);
CREATE INDEX idx_bookings_acceptance_token ON bookings(acceptance_token);

CREATE INDEX idx_booking_bookmarks_booking_id ON booking_bookmarks(booking_id);
CREATE INDEX idx_booking_bookmarks_student_id ON booking_bookmarks(student_id);

CREATE INDEX idx_booking_notes_booking_id ON booking_notes(booking_id);
CREATE INDEX idx_booking_notes_author_id ON booking_notes(author_id);

CREATE INDEX idx_teacher_slots_teacher_id ON teacher_availability_slots(teacher_id);
CREATE INDEX idx_teacher_slots_start_time ON teacher_availability_slots(start_time);
CREATE INDEX idx_teacher_slots_end_time ON teacher_availability_slots(end_time);

-- Add booking_id column to fee_waivers if it doesn't exist (it should already exist from V7)
-- This is just to ensure it exists
DO $$ 
BEGIN
    IF NOT EXISTS (SELECT 1 FROM information_schema.columns 
                   WHERE table_name = 'fee_waivers' AND column_name = 'booking_id') THEN
        ALTER TABLE fee_waivers ADD COLUMN booking_id BIGINT REFERENCES bookings(id);
        CREATE INDEX idx_fee_waivers_booking_id ON fee_waivers(booking_id);
    END IF;
END $$;

-- Create function to update updated_at timestamp
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
    NEW.updated_at = NOW();
    RETURN NEW;
END;
$$ language 'plpgsql';

-- Create triggers for updated_at
CREATE TRIGGER update_bookings_updated_at BEFORE UPDATE ON bookings
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_booking_notes_updated_at BEFORE UPDATE ON booking_notes
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER update_teacher_slots_updated_at BEFORE UPDATE ON teacher_availability_slots
    FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();
