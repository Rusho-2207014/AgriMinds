-- Add accepted_date column to price_negotiations table
USE agriminds_db;

ALTER TABLE price_negotiations 
ADD COLUMN accepted_date TIMESTAMP NULL AFTER status;

-- Optionally, set accepted_date for existing Accepted records to their updated_date
UPDATE price_negotiations 
SET accepted_date = updated_date 
WHERE status = 'Accepted' AND accepted_date IS NULL;

SELECT 'Column accepted_date added successfully!' AS Result;
