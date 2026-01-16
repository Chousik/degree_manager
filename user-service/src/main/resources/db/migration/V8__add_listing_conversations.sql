ALTER TABLE conversation
    ADD COLUMN IF NOT EXISTS listing_id UUID REFERENCES listing(id) ON DELETE CASCADE;

ALTER TABLE conversation
    ALTER COLUMN rental_id DROP NOT NULL;

CREATE INDEX IF NOT EXISTS idx_conversation_listing ON conversation(listing_id);

ALTER TABLE conversation
    ADD CONSTRAINT chk_conversation_target
    CHECK (rental_id IS NOT NULL OR listing_id IS NOT NULL);
