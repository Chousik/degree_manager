-- Seed demo users, listings, categories links, and MinIO-hosted photos (aligned with current schema)
WITH desired_users(id, email, username, name, surname, last_name, phone, rating, status, city) AS (
    VALUES
        ('80000000-0000-0000-0000-000000000001'::uuid, 'anna.renter@example.com', 'anna.renter', 'Анна', 'Иванова', 'Сергеевна', '900000001', 4.8, 'ACTIVE', 'Москва'),
        ('80000000-0000-0000-0000-000000000002'::uuid, 'pavel.tools@example.com', 'pavel.tools', 'Павел', 'Петров', 'Алексеевич', '900000002', 4.6, 'ACTIVE', 'Санкт-Петербург'),
        ('80000000-0000-0000-0000-000000000003'::uuid, 'svetlana.clean@example.com', 'svetlana.clean', 'Светлана', 'Сидорова', 'Игоревна', '900000003', 4.9, 'ACTIVE', 'Москва')
),
inserted_users AS (
    INSERT INTO "user" (id, email, username, name, surname, last_name, phone, rating, status, city)
    SELECT du.id, du.email, du.username, du.name, du.surname, du.last_name,
           du.phone, du.rating, du.status, du.city
    FROM desired_users du
    ON CONFLICT (email) DO NOTHING
    RETURNING id, email
),
all_users AS (
    SELECT id, email FROM inserted_users
    UNION
    SELECT id, email FROM "user" WHERE email IN (SELECT email FROM desired_users)
),
listing_data(id, owner_email, title, description, price_per_hour, deposit_amount, auto_confirmation, status, latitude, longitude) AS (
    VALUES
        ('71000000-0000-0000-0000-000000000001'::uuid, 'anna.renter@example.com',
         'Городской электросамокат', 'Легкий самокат с запасом хода 30 км, зарядка включена.', 350.00, 1000.00, true, 'AVAILABLE', 55.752220, 37.615560),
        ('71000000-0000-0000-0000-000000000002'::uuid, 'pavel.tools@example.com',
         'Отбойный молоток SDS-max', 'Профессиональный инструмент, выдаю с чемоданом и смазкой.', 500.00, 2000.00, false, 'AVAILABLE', 59.931100, 30.360900),
        ('71000000-0000-0000-0000-000000000003'::uuid, 'svetlana.clean@example.com',
         'Генеральная уборка 2-комнатной квартиры', 'Привезу расходники, работаю по чек-листу, скидка за повторные заказы.', 800.00, NULL, true, 'AVAILABLE', 55.995000, 37.190000)
),
inserted_listings AS (
    INSERT INTO listing (id, owner_id, title, description, price_per_hour, deposit_amount, auto_confirmation, status, latitude, longitude)
    SELECT ld.id, u.id, ld.title, ld.description, ld.price_per_hour, ld.deposit_amount,
           ld.auto_confirmation, ld.status, ld.latitude, ld.longitude
    FROM listing_data ld
    JOIN all_users u ON u.email = ld.owner_email
    ON CONFLICT (id) DO NOTHING
    RETURNING id
),
all_listings AS (
    SELECT id FROM inserted_listings
    UNION
    SELECT id FROM listing WHERE id IN (SELECT id FROM listing_data)
),
listing_categories(listing_id, category_id) AS (
    VALUES
        ('71000000-0000-0000-0000-000000000001'::uuid, '30000000-0000-0000-0000-000000000001'::uuid), -- sport-bikes
        ('71000000-0000-0000-0000-000000000002'::uuid, '20000000-0000-0000-0000-000000000002'::uuid), -- home-tools
        ('71000000-0000-0000-0000-000000000003'::uuid, '40000000-0000-0000-0000-000000000002'::uuid)  -- services-cleaning
)
INSERT INTO listing_category (listing_id, category_id)
SELECT lc.listing_id, lc.category_id
FROM listing_categories lc
WHERE lc.listing_id IN (SELECT id FROM all_listings)
  AND NOT EXISTS (
      SELECT 1 FROM listing_category lc2
      WHERE lc2.listing_id = lc.listing_id AND lc2.category_id = lc.category_id
  );

-- Photos stored in S3 (public URLs)
WITH photo_data(listing_id, url, sort_order) AS (
    VALUES
        ('71000000-0000-0000-0000-000000000001'::uuid, 'http://localhost:9000/media/work-tool-blue-background-hand-tool-new-set-repair-construction-overhead_771335-2453.jpg', 1),
        ('71000000-0000-0000-0000-000000000001'::uuid, 'http://localhost:9000/media/work-tools-on-the-wooden-background_220873-8575.jpg', 2),
        ('71000000-0000-0000-0000-000000000002'::uuid, 'http://localhost:9000/media/tools_909293-2254.jpg', 1),
        ('71000000-0000-0000-0000-000000000002'::uuid, 'http://localhost:9000/media/carpentry-construction-collage-tools-underneath-wooden-planks_488220-29813.jpg', 2),
        ('71000000-0000-0000-0000-000000000003'::uuid, 'http://localhost:9000/media/top-view-mechanical-tools-arrangement_23-2149552411.jpg', 1),
        ('71000000-0000-0000-0000-000000000003'::uuid, 'http://localhost:9000/media/232.jpg', 2)
)
INSERT INTO listing_photo (id, listing_id, url, sort_order)
SELECT gen_random_uuid(), pd.listing_id, pd.url, pd.sort_order
FROM photo_data pd
WHERE pd.listing_id IN (SELECT id FROM listing)
  AND NOT EXISTS (
      SELECT 1 FROM listing_photo lp
      WHERE lp.listing_id = pd.listing_id AND lp.url = pd.url
  );
