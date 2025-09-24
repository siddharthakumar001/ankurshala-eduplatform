-- V1__users_and_profiles.sql
-- PostgreSQL schema for AnkurShala authentication and profile management

-- USERS
CREATE TABLE users (
  id               BIGSERIAL PRIMARY KEY,
  name             VARCHAR(100) NOT NULL,
  email            VARCHAR(150) NOT NULL UNIQUE,
  password         VARCHAR(255) NOT NULL,
  role             VARCHAR(16)  NOT NULL CHECK (role IN ('STUDENT','TEACHER','ADMIN')),
  enabled          BOOLEAN NOT NULL DEFAULT TRUE,
  created_at       TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at       TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_users_role ON users(role);
CREATE INDEX idx_users_email ON users(email);

-- REFRESH TOKENS
CREATE TABLE refresh_tokens (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL REFERENCES users(id) ON DELETE CASCADE,
  token_hash VARCHAR(255) NOT NULL,
  expires_at TIMESTAMP NOT NULL,
  revoked_at TIMESTAMP NULL,
  created_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_refresh_user ON refresh_tokens(user_id);
CREATE INDEX idx_refresh_token_hash ON refresh_tokens(token_hash);

-- STUDENT PROFILES
CREATE TABLE student_profiles (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  first_name VARCHAR(100) NOT NULL,
  middle_name VARCHAR(100),
  last_name VARCHAR(100) NOT NULL,
  mother_name VARCHAR(100),
  father_name VARCHAR(100),
  guardian_name VARCHAR(100),
  parent_name VARCHAR(100),
  mobile_number VARCHAR(20),
  alternate_mobile_number VARCHAR(20),
  date_of_birth DATE,
  educational_board VARCHAR(20) CHECK (educational_board IN ('CBSE','ICSE','STATE_BOARD','IB','CAMBRIDGE','OTHER')),
  class_level VARCHAR(16), -- e.g., GRADE_7..GRADE_12
  grade_level VARCHAR(20),
  school_name VARCHAR(200),
  emergency_contact VARCHAR(100),
  student_photo_url VARCHAR(500),
  school_id_card_url VARCHAR(500),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE student_documents (
  id BIGSERIAL PRIMARY KEY,
  student_id BIGINT NOT NULL REFERENCES student_profiles(id) ON DELETE CASCADE,
  document_name VARCHAR(100) NOT NULL,
  document_url VARCHAR(500) NOT NULL,
  document_type VARCHAR(50),
  upload_date TIMESTAMP NOT NULL DEFAULT NOW(),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_student_docs_sid ON student_documents(student_id);

-- TEACHER (core) + TEACHER PROFILE
CREATE TABLE teachers (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  name VARCHAR(255) NOT NULL,
  email VARCHAR(255) NOT NULL,
  phone_number VARCHAR(20),
  alternate_phone_number VARCHAR(20),
  date_of_birth DATE,
  gender VARCHAR(10) CHECK (gender IN ('MALE','FEMALE','OTHER')),
  profile_picture_url VARCHAR(500),
  linkedin_profile VARCHAR(255),
  bio TEXT,
  status VARCHAR(16) NOT NULL DEFAULT 'PENDING' CHECK (status IN ('PENDING','ACTIVE','INACTIVE','SUSPENDED')),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE teacher_profiles (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  teacher_id BIGINT NOT NULL UNIQUE REFERENCES teachers(id) ON DELETE CASCADE,
  bio TEXT,
  qualifications TEXT,
  hourly_rate DECIMAL(8,2),
  years_of_experience INTEGER,
  specialization VARCHAR(255),
  verified BOOLEAN DEFAULT FALSE,
  first_name VARCHAR(100),
  middle_name VARCHAR(100),
  last_name VARCHAR(100),
  mobile_number VARCHAR(20),
  alternate_mobile_number VARCHAR(20),
  contact_email VARCHAR(150),
  highest_education VARCHAR(200),
  postal_address VARCHAR(500),
  city VARCHAR(100),
  state VARCHAR(100),
  country VARCHAR(100) DEFAULT 'India',
  secondary_address VARCHAR(500),
  profile_photo_url VARCHAR(500),
  govt_id_proof_url VARCHAR(500),
  rating DECIMAL(3,2) DEFAULT 0.0,
  total_reviews INTEGER DEFAULT 0,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- PROFESSIONAL INFO
CREATE TABLE teacher_professional_info (
  id BIGSERIAL PRIMARY KEY,
  teacher_id BIGINT NOT NULL UNIQUE REFERENCES teachers(id) ON DELETE CASCADE,
  specialization VARCHAR(255),
  years_of_experience INTEGER,
  hourly_rate DECIMAL(8,2),
  current_institution VARCHAR(255),
  designation VARCHAR(255),
  subjects_expertise TEXT,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- QUALIFICATIONS / EXPERIENCE / CERTIFICATIONS
CREATE TABLE teacher_qualifications (
  id BIGSERIAL PRIMARY KEY,
  teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
  degree VARCHAR(255),
  specialization VARCHAR(255),
  university VARCHAR(255),
  year INTEGER,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_tqual_tid ON teacher_qualifications(teacher_id);

CREATE TABLE teacher_experiences (
  id BIGSERIAL PRIMARY KEY,
  teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
  institution VARCHAR(255),
  role VARCHAR(255),
  subjects_taught TEXT,
  from_date DATE,
  to_date DATE,
  currently_working BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_texp_tid ON teacher_experiences(teacher_id);

CREATE TABLE teacher_certifications (
  id BIGSERIAL PRIMARY KEY,
  teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
  certification_name VARCHAR(255),
  issuing_authority VARCHAR(255),
  certification_id VARCHAR(100),
  issue_year INTEGER,
  expiry_date DATE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_tcert_tid ON teacher_certifications(teacher_id);

-- AVAILABILITY (profile-level, separate booking slots will come later)
CREATE TABLE teacher_availability (
  id BIGSERIAL PRIMARY KEY,
  teacher_id BIGINT NOT NULL UNIQUE REFERENCES teachers(id) ON DELETE CASCADE,
  available_from TIME,
  available_to TIME,
  preferred_student_levels JSONB,
  languages_spoken JSONB,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- DOCUMENTS
CREATE TABLE teacher_documents (
  id BIGSERIAL PRIMARY KEY,
  teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
  document_name VARCHAR(255),
  document_url VARCHAR(500) NOT NULL,
  document_type VARCHAR(50),
  upload_date TIMESTAMP NOT NULL DEFAULT NOW(),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_tdocs_tid ON teacher_documents(teacher_id);

-- BANK DETAILS (account_number encrypted in app)
CREATE TABLE teacher_bank_details (
  id BIGSERIAL PRIMARY KEY,
  teacher_id BIGINT NOT NULL UNIQUE REFERENCES teachers(id) ON DELETE CASCADE,
  pan_number VARCHAR(20),
  account_holder_name VARCHAR(255),
  bank_name VARCHAR(255),
  account_number VARCHAR(500),
  ifsc_code VARCHAR(11),
  account_type VARCHAR(16) CHECK (account_type IN ('SAVINGS','CURRENT','CHECKING')),
  branch_address TEXT,
  micr_code VARCHAR(15),
  mobile_number VARCHAR(20),
  email VARCHAR(255),
  terms_accepted BOOLEAN DEFAULT FALSE,
  verified BOOLEAN DEFAULT FALSE,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- ADDRESSES
CREATE TABLE teacher_addresses (
  id BIGSERIAL PRIMARY KEY,
  teacher_id BIGINT NOT NULL REFERENCES teachers(id) ON DELETE CASCADE,
  address_line1 VARCHAR(255),
  address_line2 VARCHAR(255),
  city VARCHAR(100),
  state VARCHAR(100),
  zip_code VARCHAR(20),
  country VARCHAR(100) DEFAULT 'India',
  address_type VARCHAR(16) CHECK (address_type IN ('PERMANENT','CURRENT')),
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);
CREATE INDEX idx_taddr_tid ON teacher_addresses(teacher_id);

-- ADMIN PROFILE
CREATE TABLE admin_profiles (
  id BIGSERIAL PRIMARY KEY,
  user_id BIGINT NOT NULL UNIQUE REFERENCES users(id) ON DELETE CASCADE,
  phone_number VARCHAR(20),
  is_super_admin BOOLEAN DEFAULT FALSE,
  last_login TIMESTAMP,
  created_at TIMESTAMP NOT NULL DEFAULT NOW(),
  updated_at TIMESTAMP NOT NULL DEFAULT NOW()
);

-- Insert default admin user (password: admin123)
INSERT INTO users (name, email, password, role, enabled) VALUES 
('Admin User', 'admin@ankurshala.com', '$2a$12$LQv3c1yqBWVHxkd0LHAkCOYz6TtxMQJqhN8/LewdBPj4/LewdBPj4', 'ADMIN', true);

INSERT INTO admin_profiles (user_id, phone_number, is_super_admin) VALUES 
(1, '+91-9999999999', true);
