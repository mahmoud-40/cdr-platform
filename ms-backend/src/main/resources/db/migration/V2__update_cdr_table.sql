-- Drop old columns
ALTER TABLE cdrs DROP COLUMN call_status;
ALTER TABLE cdrs DROP COLUMN call_type;
ALTER TABLE cdrs DROP COLUMN calling_number;
ALTER TABLE cdrs DROP COLUMN called_number;
ALTER TABLE cdrs DROP COLUMN duration_seconds;
ALTER TABLE cdrs DROP COLUMN end_time;

-- Add new columns
ALTER TABLE cdrs ADD COLUMN source VARCHAR(255) NOT NULL;
ALTER TABLE cdrs ADD COLUMN destination VARCHAR(255) NOT NULL;
ALTER TABLE cdrs ADD COLUMN service VARCHAR(50) NOT NULL;
ALTER TABLE cdrs ADD COLUMN usage INT NOT NULL DEFAULT 0; 