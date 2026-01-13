ALTER TABLE report DROP CONSTRAINT IF EXISTS report_target_id_fkey;
ALTER TABLE report DROP COLUMN IF EXISTS listing_id;
ALTER TABLE report DROP COLUMN IF EXISTS review_id;
ALTER TABLE report DROP COLUMN IF EXISTS rental_id;

CREATE INDEX IF NOT EXISTS idx_report_target_type ON report(target_type);
CREATE INDEX IF NOT EXISTS idx_report_target_id ON report(target_id);
