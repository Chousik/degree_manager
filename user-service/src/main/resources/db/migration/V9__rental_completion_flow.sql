ALTER TABLE rental
    ADD COLUMN IF NOT EXISTS completion_requested_by UUID,
    ADD COLUMN IF NOT EXISTS completion_requested_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS completion_confirmed_by UUID,
    ADD COLUMN IF NOT EXISTS completion_confirmed_at TIMESTAMPTZ,
    ADD COLUMN IF NOT EXISTS cancellation_requested_by UUID,
    ADD COLUMN IF NOT EXISTS cancellation_requested_at TIMESTAMPTZ;
