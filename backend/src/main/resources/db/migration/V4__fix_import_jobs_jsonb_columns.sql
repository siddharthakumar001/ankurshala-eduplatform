-- Fix import_jobs table JSONB columns to TEXT for easier handling
ALTER TABLE import_jobs 
ALTER COLUMN stats TYPE TEXT USING stats::TEXT;

ALTER TABLE import_jobs 
ALTER COLUMN errors TYPE TEXT USING errors::TEXT;
