CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS pg_cron;

CREATE TABLE "user" (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    email VARCHAR(100) NOT NULL UNIQUE,
    name VARCHAR(100) NOT NULL,
    phone VARCHAR(10),
    rating DECIMAL(2,1),
    city VARCHAR(60) NOT NULL DEFAULT 'Москва',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE notification (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    type VARCHAR(50),
    body TEXT,
    is_read BOOLEAN DEFAULT FALSE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_notification_user_id ON notification(user_id);

CREATE TABLE blacklist (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID REFERENCES "user"(id) ON DELETE CASCADE,
    blocked_user_id UUID REFERENCES "user"(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_blacklist_owner ON blacklist(owner_id);
CREATE INDEX idx_blacklist_blocked ON blacklist(blocked_user_id);

CREATE TABLE listing (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    owner_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    title VARCHAR(500) NOT NULL,
    description TEXT,
    price_per_hour DECIMAL(38,10) NOT NULL,
    deposit_amount DECIMAL(38,10),
    auto_confirmation BOOLEAN DEFAULT FALSE,
    status VARCHAR(30) DEFAULT 'active',
    latitude DECIMAL(9,6),
    longitude DECIMAL(9,6),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_listing_owner_id ON listing(owner_id);
CREATE INDEX idx_listing_status ON listing(status);

CREATE TABLE listing_photo (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id UUID REFERENCES listing(id) ON DELETE CASCADE,
    url VARCHAR(255) NOT NULL,
    sort_order SMALLINT
);
CREATE INDEX idx_listing_photo_listing_id ON listing_photo(listing_id);

CREATE TABLE category (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    parent_id UUID REFERENCES category(id) ON DELETE SET NULL,
    name VARCHAR(100) NOT NULL,
    url_name VARCHAR(100) UNIQUE
);

CREATE TABLE listing_category (
    listing_id UUID REFERENCES listing(id) ON DELETE CASCADE,
    category_id UUID REFERENCES category(id) ON DELETE CASCADE,
    PRIMARY KEY (listing_id, category_id)
);

CREATE TABLE favorite (
    user_id UUID REFERENCES "user"(id) ON DELETE CASCADE,
    listing_id UUID REFERENCES listing(id) ON DELETE CASCADE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (user_id, listing_id)
);
CREATE INDEX idx_favorite_listing ON favorite(listing_id);

CREATE TABLE availability_slot (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id UUID NOT NULL REFERENCES listing(id) ON DELETE CASCADE,
    starts_at TIMESTAMP NOT NULL,
    ends_at TIMESTAMP NOT NULL,
    note VARCHAR(255)
);
CREATE INDEX idx_av_slot_listing ON availability_slot(listing_id);
CREATE INDEX idx_av_slot_range ON availability_slot(starts_at, ends_at);

CREATE TABLE rental (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    listing_id UUID NOT NULL REFERENCES listing(id) ON DELETE CASCADE,
    lessor_id UUID REFERENCES "user"(id),
    lessee_id UUID REFERENCES "user"(id),
    start_at TIMESTAMP NOT NULL,
    end_at TIMESTAMP NOT NULL,
    status VARCHAR(30) DEFAULT 'pending',
    total_amount DECIMAL(38,10),
    deposit_amount DECIMAL(38,10),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_rental_status ON rental(status);
CREATE INDEX idx_rental_listing_start_end ON rental(listing_id, start_at, end_at);
CREATE INDEX idx_rental_start_end ON rental(start_at, end_at);

CREATE TABLE contract (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rental_id UUID REFERENCES rental(id) ON DELETE CASCADE,
    status VARCHAR(30),
    signed_at TIMESTAMP,
    file_url VARCHAR(255),
    signature_hash VARCHAR(255)
);
CREATE INDEX idx_contract_rental ON contract(rental_id);

CREATE TABLE payment (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rental_id UUID REFERENCES rental(id) ON DELETE CASCADE,
    amount DECIMAL(38,10) NOT NULL,
    status VARCHAR(20),
    paid_at TIMESTAMP,
    external_id VARCHAR(100),
    purpose VARCHAR(20),
    currency VARCHAR(3),
    confirmation_url VARCHAR(500),
    refunded_at TIMESTAMP
);
CREATE INDEX idx_payment_rental ON payment(rental_id);

CREATE TABLE notification_preference (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    user_id UUID UNIQUE REFERENCES "user"(id) ON DELETE CASCADE,
    system_notifications BOOLEAN,
    rental_notifications BOOLEAN,
    message_notifications BOOLEAN,
    payment_notifications BOOLEAN,
    updated_at TIMESTAMP
);

CREATE TABLE support_ticket (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    requester_id UUID REFERENCES "user"(id) ON DELETE CASCADE,
    rental_id UUID REFERENCES rental(id) ON DELETE SET NULL,
    status VARCHAR(30),
    subject VARCHAR(255),
    message TEXT,
    created_at TIMESTAMP,
    resolved_at TIMESTAMP,
    resolution_notes TEXT
);
CREATE INDEX idx_support_ticket_status ON support_ticket(status);

CREATE TABLE review (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    lessor_id UUID REFERENCES "user"(id) ON DELETE SET NULL,
    lessee_id UUID REFERENCES "user"(id) ON DELETE SET NULL,
    listing_id UUID REFERENCES listing(id) ON DELETE SET NULL,
    rental_id UUID REFERENCES rental(id) ON DELETE SET NULL,
    author_role VARCHAR(20),
    rating SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
    text TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_review_lessor ON review(lessor_id);
CREATE INDEX idx_review_lessee ON review(lessee_id);
CREATE INDEX idx_review_listing ON review(listing_id);

CREATE TABLE conversation (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    rental_id UUID NOT NULL REFERENCES rental(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_conversation_rental ON conversation(rental_id);

CREATE TABLE conversation_pair (
    conversation_id UUID NOT NULL REFERENCES conversation(id) ON DELETE CASCADE,
    user_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    PRIMARY KEY (conversation_id, user_id)
);
CREATE INDEX idx_convpair_user ON conversation_pair(user_id);

CREATE TABLE message (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    conversation_id UUID NOT NULL REFERENCES conversation(id) ON DELETE CASCADE,
    sender_id UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
    body TEXT NOT NULL,
    sent_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    is_read BOOLEAN DEFAULT FALSE
);
CREATE INDEX idx_message_conversation ON message(conversation_id);
CREATE INDEX idx_message_sender ON message(sender_id);

CREATE TABLE report (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    reporter_id UUID REFERENCES "user"(id) ON DELETE SET NULL,
    status VARCHAR(30) DEFAULT 'open',
    target_type VARCHAR(30),
    target_id UUID REFERENCES rental(id),
    reason_body TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    resolved_by UUID REFERENCES "user"(id) ON DELETE SET NULL
);
CREATE INDEX idx_report_reporter ON report(reporter_id);
CREATE INDEX idx_report_status ON report(status);

CREATE TABLE moderation_action (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    report_id UUID REFERENCES report(id) ON DELETE CASCADE,
    listing_id UUID REFERENCES listing(id) ON DELETE SET NULL,
    actor_id UUID REFERENCES "user"(id) ON DELETE SET NULL,
    action VARCHAR(50),
    comment TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_modact_report ON moderation_action(report_id);
CREATE INDEX idx_modact_listing ON moderation_action(listing_id);
CREATE INDEX idx_modact_actor ON moderation_action(actor_id);

CREATE TABLE notification_preference_change (
    id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
    preference_id UUID REFERENCES notification_preference(id) ON DELETE CASCADE,
    changed_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    snapshot JSONB
);

CREATE OR REPLACE FUNCTION fn_mark_expired_rentals() RETURNS void
    LANGUAGE plpgsql AS $$
DECLARE
    rec RECORD;
BEGIN
    WITH expired AS (
        UPDATE rental r
            SET status = 'expired'
            WHERE r.end_at <= now() AND r.status IN ('pending','active')
            RETURNING r.id, r.lessor_id, r.lessee_id, r.listing_id
    )
    INSERT INTO notification(id, user_id, type, body, created_at)
    SELECT gen_random_uuid(),
           u,
           'rental_expired',
           CONCAT('Rental ', e.id::text, ' for listing ', e.listing_id::text, ' has expired.'),
           now()
    FROM expired e
             CROSS JOIN LATERAL (VALUES (e.lessor_id), (e.lessee_id)) AS v(u)
    WHERE v.u IS NOT NULL;

    FOR rec IN
        SELECT DISTINCT listing_id FROM rental WHERE status = 'expired' AND end_at <= now()
        LOOP
            IF NOT EXISTS (
                SELECT 1 FROM rental r2
                WHERE r2.listing_id = rec.listing_id AND r2.status IN ('pending','active')
            ) THEN
                UPDATE listing
                SET status = 'active'
                WHERE id = rec.listing_id;
            END IF;
        END LOOP;
END;
$$;
SELECT cron.schedule('mark_expired_rentals_hourly', '0 * * * *', $$SELECT fn_mark_expired_rentals();$$);
