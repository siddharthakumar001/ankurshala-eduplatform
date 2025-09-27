-- Fee Waivers Migration
-- Create fee_waivers table for admin fee waiver actions

CREATE TABLE IF NOT EXISTS fee_waivers (
    id BIGSERIAL PRIMARY KEY,
    booking_id BIGINT NULL, -- Will be used when bookings are implemented
    user_id BIGINT REFERENCES users(id) NOT NULL,
    reason VARCHAR(500) NOT NULL,
    amount DECIMAL(8,2) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

-- Create indexes for efficient lookups
CREATE INDEX idx_fee_waivers_user_id ON fee_waivers(user_id);
CREATE INDEX idx_fee_waivers_booking_id ON fee_waivers(booking_id);
CREATE INDEX idx_fee_waivers_created_at ON fee_waivers(created_at);
