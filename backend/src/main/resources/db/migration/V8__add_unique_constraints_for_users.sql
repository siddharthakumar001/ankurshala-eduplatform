-- Add unique constraints to prevent duplicate users based on email + mobile combination
-- This ensures that the same email or mobile number cannot be used by multiple users

-- Add unique constraint on mobile_number in student_profiles
-- Only apply constraint where mobile_number is not null
CREATE UNIQUE INDEX idx_student_profiles_mobile_unique 
ON student_profiles (mobile_number) 
WHERE mobile_number IS NOT NULL AND mobile_number != '';

-- Add unique constraint on mobile_number in teacher_profiles  
-- Only apply constraint where mobile_number is not null
CREATE UNIQUE INDEX idx_teacher_profiles_mobile_unique 
ON teacher_profiles (mobile_number) 
WHERE mobile_number IS NOT NULL AND mobile_number != '';

-- Add unique constraint on alternate_mobile_number in student_profiles
-- Only apply constraint where alternate_mobile_number is not null
CREATE UNIQUE INDEX idx_student_profiles_alt_mobile_unique 
ON student_profiles (alternate_mobile_number) 
WHERE alternate_mobile_number IS NOT NULL AND alternate_mobile_number != '';

-- Add unique constraint on alternate_mobile_number in teacher_profiles
-- Only apply constraint where alternate_mobile_number is not null  
CREATE UNIQUE INDEX idx_teacher_profiles_alt_mobile_unique 
ON teacher_profiles (alternate_mobile_number) 
WHERE alternate_mobile_number IS NOT NULL AND alternate_mobile_number != '';

-- Add unique constraint on contact_email in teacher_profiles
-- Only apply constraint where contact_email is not null
CREATE UNIQUE INDEX idx_teacher_profiles_contact_email_unique 
ON teacher_profiles (contact_email) 
WHERE contact_email IS NOT NULL AND contact_email != '';

-- Note: The users table already has a unique constraint on email
-- This migration adds mobile number uniqueness constraints to prevent
-- duplicate users based on mobile numbers across student and teacher profiles
