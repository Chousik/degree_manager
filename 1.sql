CREATE EXTENSION IF NOT EXISTS "pgcrypto";
CREATE EXTENSION IF NOT EXISTS pg_cron;

CREATE TABLE IF NOT EXISTS "user" (
                  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  email        CITEXT NOT NULL UNIQUE,
                  name         VARCHAR(20) NOT NULL,
                  surname      VARCHAR(60) NOT NULL,
                  last_name    VARCHAR(20),
                  phone        VARCHAR(12),
                  rating       DECIMAL(2,1),
                  status       VARCHAR(20) NOT NULL DEFAULT 'active',
                  last_ban     INT REFERENCES ban_list(id),
                  created_at   TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  CONSTRAINT chk_user_phone_format CHECK (phone IS NULL OR phone ~ '^\d{10,12}$'),
                  CONSTRAINT chk_user_rating CHECK (rating IS NULL OR (rating >= 0 AND rating <= 5)),
                  CONSTRAINT chk_user_status CHECK (status IN ('active','banned','deleted'))
                  CONSTRAINT chk_user_email_format CHECK (email ~* '^[A-Z0-9._%+-]+@[A-Z0-9.-]+\.[A-Z]{2,}$')
                  CONSTRAINT chk_user_ban_fields CHECK ( (status = 'banned') = (banned_at IS NOT NULL) AND (status <> 'banned' OR ban_reason IS NOT NULL) )
              );

              CREATE TABLE IF NOT EXISTS ban_list (
                  id              UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  banned_user_id  INT NOT NULL REFERENCES "user"(id),
                  ban_reason      VARCHAR(1000),
                  ban_type        VARCHAR(30),
                  ban_duration    TIMESTAMPTZ,
                  admin_user_id   INT NOT NULL REFERENCES "user"(id),
                  status          VARCHAR(20) NOT NULL CHECK (status IN ('active', 'canceled')) DEFAULT 'active',
              );

              CREATE TABLE IF NOT EXISTS notification (
                  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  user_id      UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
                  type         VARCHAR(10),
                  body         TEXT,
                  is_read      BOOLEAN DEFAULT FALSE,
                  created_at   TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                  CONSTRAINT chk_notification_type CHECK (type IN ('system','message','rental','payment','moderation'))
              );

              CREATE TABLE IF NOT EXISTS blacklist (
                  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  owner_id         UUID REFERENCES "user"(id) ON DELETE CASCADE,
                  blocked_user_id  UUID REFERENCES "user"(id) ON DELETE CASCADE,
                  created_at       TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                  CONSTRAINT uq_blacklist_owner_blocked UNIQUE (owner_id, blocked_user_id),
                  CONSTRAINT chk_blacklist_not_self CHECK (owner_id <> blocked_user_id)
              );

              CREATE UNIQUE INDEX IF NOT EXISTS ux_blacklist_owner_blocked
                  ON blacklist(owner_id, blocked_user_id);

              CREATE TABLE IF NOT EXISTS listing (
                  id                 UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  owner_id           UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
                  title              VARCHAR(500) NOT NULL,
                  description        TEXT,
                  price_per_hour     DECIMAL(38,10) NOT NULL,
                  deposit_amount     DECIMAL(38,10),
                  auto_confirmation  BOOLEAN DEFAULT FALSE,
                  status             VARCHAR(30) DEFAULT 'active',
                  latitude           DECIMAL(9,6),
                  longitude          DECIMAL(9,6),
                  created_at         TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                  CONSTRAINT chk_listing_price_nonneg   CHECK (price_per_hour IS NULL OR price_per_hour >= 0),
                  CONSTRAINT chk_listing_deposit_nonneg CHECK (deposit_amount IS NULL OR deposit_amount >= 0),
                  CONSTRAINT chk_listing_lat            CHECK (latitude  IS NULL OR (latitude  >= -90  AND latitude  <= 90)),
                  CONSTRAINT chk_listing_lon            CHECK (longitude IS NULL OR (longitude >= -180 AND longitude <= 180)),
                  CONSTRAINT chk_listing_status         CHECK (status IN ('active','paused','archived','blocked'))
              );

              CREATE TABLE IF NOT EXISTS listing_photo (
                  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  listing_id   UUID REFERENCES listing(id) ON DELETE CASCADE,
                  url          VARCHAR(255) NOT NULL,
                  sort_order   SMALLINT,
                  CONSTRAINT uq_photo_listing_sort UNIQUE (listing_id, sort_order)
              );

              CREATE TABLE IF NOT EXISTS category (
                  id         UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  parent_id  UUID REFERENCES category(id) ON DELETE SET NULL,
                  name       VARCHAR(100) NOT NULL,
                  url_name   VARCHAR(100) UNIQUE
              );

              CREATE TABLE IF NOT EXISTS listing_category (
                  listing_id   UUID REFERENCES listing(id) ON DELETE CASCADE,
                  category_id  UUID REFERENCES category(id) ON DELETE CASCADE,
                  PRIMARY KEY (listing_id, category_id)
              );

              CREATE TABLE IF NOT EXISTS favorite (
                  user_id     UUID REFERENCES "user"(id) ON DELETE CASCADE,
                  listing_id  UUID REFERENCES listing(id) ON DELETE CASCADE,
                  created_at  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                  PRIMARY KEY (user_id, listing_id)
              );

              CREATE TABLE IF NOT EXISTS availability_slot (
                  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  listing_id  UUID NOT NULL REFERENCES listing(id) ON DELETE CASCADE,
                  starts_at   TIMESTAMPTZ NOT NULL,
                  ends_at     TIMESTAMPTZ NOT NULL,
                  note        VARCHAR(255)
              );

              CREATE TABLE IF NOT EXISTS rental (
                  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  listing_id     UUID NOT NULL REFERENCES listing(id) ON DELETE CASCADE,
                  lessor_id      UUID REFERENCES "user"(id),
                  lessee_id      UUID REFERENCES "user"(id),
                  start_at       TIMESTAMPTZ NOT NULL,
                  end_at         TIMESTAMPTZ NOT NULL,
                  status         VARCHAR(30) DEFAULT 'pending',
                  total_amount   DECIMAL(38,10),
                  deposit_amount DECIMAL(38,10),
                  created_at     TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                  period         tsrange GENERATED ALWAYS AS (tsrange(start_at, end_at, '[)')) STORED,
                  CONSTRAINT chk_rental_status CHECK (status IN ('pending','active','cancelled','expired','completed'))
              );

              CREATE TABLE IF NOT EXISTS payment (
                  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  rental_id   UUID REFERENCES rental(id) ON DELETE CASCADE,
                  amount      DECIMAL(38,10) NOT NULL,
                  status      VARCHAR(20) DEFAULT 'unpaid',
                  paid_at     TIMESTAMPTZ,
                  external_id VARCHAR(100),
                  CONSTRAINT chk_payment_amount_nonneg CHECK (amount >= 0),
                  CONSTRAINT chk_payment_status CHECK (status IN ('unpaid','paid','refunded','void'))
              );

              CREATE TABLE IF NOT EXISTS contract (
                  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  rental_id      UUID REFERENCES rental(id) ON DELETE CASCADE,
                  status         VARCHAR(30) DEFAULT 'draft',
                  signed_at      TIMESTAMPTZ,
                  file_url       VARCHAR(255),
                  signature_hash VARCHAR(255),
                  CONSTRAINT chk_contract_status CHECK (status IN ('draft','sent','signed','cancelled'))
              );

              CREATE TABLE IF NOT EXISTS review (
                  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  lessor_id   UUID REFERENCES "user"(id) ON DELETE SET NULL,
                  lessee_id   UUID REFERENCES "user"(id) ON DELETE SET NULL,
                  listing_id  UUID REFERENCES listing(id) ON DELETE SET NULL,
                  rating      SMALLINT NOT NULL CHECK (rating BETWEEN 1 AND 5),
                  text        TEXT,
                  created_at  TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP
              );

              CREATE TABLE IF NOT EXISTS conversation (
                  id          UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  rental_id   UUID NOT NULL REFERENCES rental(id) ON DELETE CASCADE,
                  created_at  TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP
              );

              CREATE TABLE IF NOT EXISTS conversation_pair (
                  conversation_id UUID NOT NULL REFERENCES conversation(id) ON DELETE CASCADE,
                  user_id         UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
                  created_at      TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  PRIMARY KEY (conversation_id, user_id)
              );

              CREATE INDEX IF NOT EXISTS idx_convpair_user ON conversation_pair(user_id);

              CREATE TABLE IF NOT EXISTS message (
                  id               UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  conversation_id  UUID NOT NULL REFERENCES conversation(id) ON DELETE CASCADE,
                  sender_id        UUID NOT NULL REFERENCES "user"(id) ON DELETE CASCADE,
                  body             TEXT NOT NULL,
                  sent_at          TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
                  is_read          BOOLEAN DEFAULT FALSE
              );

              CREATE TABLE IF NOT EXISTS report (
                  id           UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  reporter_id  UUID REFERENCES "user"(id) ON DELETE SET NULL,
                  status       VARCHAR(30) DEFAULT 'open',
                  target_type  VARCHAR(30),
                  target_id    UUID REFERENCES rental(id),
                  reason_body  TEXT,
                  created_at   TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                  resolved_by  UUID REFERENCES "user"(id) ON DELETE SET NULL,
                  CONSTRAINT chk_report_status CHECK (status IN ('open','in_review','resolved','rejected'))
              );

              CREATE TABLE IF NOT EXISTS moderation_action (
                  id             UUID PRIMARY KEY DEFAULT gen_random_uuid(),
                  report_id      UUID REFERENCES report(id) ON DELETE CASCADE,
                  listing_id     UUID REFERENCES listing(id) ON DELETE SET NULL,
                  actor_id       UUID REFERENCES "user"(id) ON DELETE SET NULL,
                  target_user_id UUID REFERENCES "user"(id) ON DELETE SET NULL,
                  action         VARCHAR(50),
                  comment        TEXT,
                  created_at     TIMESTAMPTZ DEFAULT CURRENT_TIMESTAMP,
                  CONSTRAINT chk_moderation_action CHECK (action IN ('warn','block_user','unblock_user','hide_listing','unhide_listing','close_report'))
              );