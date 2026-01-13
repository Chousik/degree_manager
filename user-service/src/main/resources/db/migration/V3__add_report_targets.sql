ALTER TABLE report
    ADD COLUMN listing_id UUID REFERENCES listing(id) ON DELETE SET NULL,
    ADD COLUMN review_id UUID REFERENCES review(id) ON DELETE SET NULL,
    ADD COLUMN rental_id UUID REFERENCES rental(id) ON DELETE SET NULL;

CREATE INDEX idx_report_listing ON report(listing_id);
CREATE INDEX idx_report_review ON report(review_id);
CREATE INDEX idx_report_rental ON report(rental_id);

UPDATE report
SET rental_id = target_id
WHERE target_id IS NOT NULL AND rental_id IS NULL;


