BEGIN;

WITH desired_users(id, email, username, name, surname, last_name, phone, rating, status, city) AS (
    VALUES
        ('80000000-0000-0000-0000-000000000011'::uuid, 'owner.one@example.com', 'owner.one', 'Иван', 'Крылов', 'Петрович', '900000011', 4.7, 'ACTIVE', 'Москва'),
        ('80000000-0000-0000-0000-000000000012'::uuid, 'owner.two@example.com', 'owner.two', 'Ольга', 'Синицына', 'Ильинична', '900000012', 4.6, 'ACTIVE', 'Москва'),
        ('80000000-0000-0000-0000-000000000013'::uuid, 'owner.three@example.com', 'owner.three', 'Сергей', 'Волков', 'Андреевич', '900000013', 4.8, 'ACTIVE', 'Санкт-Петербург'),
        ('80000000-0000-0000-0000-000000000014'::uuid, 'owner.four@example.com', 'owner.four', 'Мария', 'Котова', 'Олеговна', '900000014', 4.5, 'ACTIVE', 'Казань'),
        ('80000000-0000-0000-0000-000000000015'::uuid, 'owner.five@example.com', 'owner.five', 'Антон', 'Соколов', 'Романович', '900000015', 4.9, 'ACTIVE', 'Екатеринбург')
),
inserted_users AS (
    INSERT INTO "user" (id, email, username, name, surname, last_name, phone, rating, status, city, created_at)
    SELECT du.id, du.email, du.username, du.name, du.surname, du.last_name,
           du.phone, du.rating, du.status, du.city, NOW()
    FROM desired_users du
    ON CONFLICT DO NOTHING
    RETURNING id
),
user_seed AS (
    SELECT 1 AS ready
    FROM inserted_users
    UNION ALL
    SELECT 1
    LIMIT 1
),
owner_list AS (
    SELECT u.id, row_number() OVER () AS rn, count(*) OVER () AS total
    FROM "user" u, user_seed
    WHERE u.email IN (
        'owner.one@example.com',
        'owner.two@example.com',
        'owner.three@example.com',
        'owner.four@example.com',
        'owner.five@example.com'
    )
),
arrays AS (
    SELECT
        ARRAY[
            'Шуруповерт аккумуляторный', 'Перфоратор SDS-max', 'УШМ 125 мм', 'Циркулярная пила',
            'Фрезер ручной', 'Лобзик', 'Газонокосилка', 'Триммер бензиновый',
            'Культиватор садовый', 'Бетономешалка', 'Генератор бензиновый', 'Виброплита',
            'Строительные леса', 'Лазерный уровень', 'Дальномер', 'Тепловизор',
            'Мультиметр', 'Домкрат гидравлический', 'Компрессор автомобильный', 'Диагностический сканер'
        ] AS titles,
        ARRAY[
            'В хорошем состоянии, комплект расходников включен.',
            'Тихая работа, удобная рукоять, выдаю с кейсом.',
            'Надежный инструмент для ремонта и строительства.',
            'Проверен, обслужен, готов к работе.',
            'Подойдет для бытовых и проф. задач.',
            'Есть инструкция, помогу с настройкой.',
            'Минимальный срок аренды — сутки.',
            'Скидка при аренде от 3 дней.',
            'Выдача рядом с метро или по договоренности.',
            'Можно забрать сегодня, есть расходники.'
        ] AS descriptions,
        ARRAY[
            'Москва, Тверская 10',
            'Москва, Ленинградский проспект 45',
            'Санкт-Петербург, Невский 120',
            'Казань, Баумана 15',
            'Екатеринбург, Ленина 50',
            'Москва, Варшавское шоссе 32',
            'Санкт-Петербург, Московский проспект 80',
            'Казань, Кремлевская 9'
        ] AS addresses,
        ARRAY[55.752220, 55.743880, 59.931100, 55.796127, 56.838926, 55.669986, 59.920000, 55.790000] AS lats,
        ARRAY[37.615560, 37.620700, 30.360900, 49.106414, 60.605703, 37.617500, 30.315000, 49.122000] AS lons
),
cleanup AS (
    DELETE FROM listing WHERE title LIKE 'Тестовое объявление %'
    RETURNING 1
),
new_listings AS (
    SELECT
        gs AS idx,
        gen_random_uuid() AS id,
        o.id AS owner_id,
        format('Тестовое объявление %s: %s', gs,
            (SELECT titles[((gs - 1) % array_length(titles, 1)) + 1] FROM arrays)
        ) AS title,
        (SELECT descriptions[((gs - 1) % array_length(descriptions, 1)) + 1] FROM arrays) AS description,
        (1000 + (gs % 10) * 150)::numeric AS price_per_hour,
        (2000 + (gs % 6) * 250)::numeric AS deposit_amount,
        (gs % 2 = 0) AS auto_confirmation,
        'AVAILABLE' AS status,
        (SELECT lats[((gs - 1) % array_length(lats, 1)) + 1] FROM arrays) AS latitude,
        (SELECT lons[((gs - 1) % array_length(lons, 1)) + 1] FROM arrays) AS longitude,
        (SELECT addresses[((gs - 1) % array_length(addresses, 1)) + 1] FROM arrays) AS address
    FROM generate_series(1, 50) gs
    JOIN owner_list o ON o.rn = ((gs - 1) % o.total) + 1
    LEFT JOIN cleanup c ON true
),
inserted_listings AS (
    INSERT INTO listing (id, owner_id, title, description, price_per_hour, deposit_amount, auto_confirmation,
                         status, latitude, longitude, address)
    SELECT id, owner_id, title, description, price_per_hour, deposit_amount, auto_confirmation,
           status, latitude, longitude, address
    FROM new_listings
    RETURNING id
),
listing_index AS (
    SELECT nl.idx, nl.id
    FROM new_listings nl
    JOIN inserted_listings il ON il.id = nl.id
),
category_arrays AS (
    SELECT ARRAY[
            '20000000-0000-0000-0000-000000000101'::uuid,
            '20000000-0000-0000-0000-000000000102'::uuid,
            '20000000-0000-0000-0000-000000000103'::uuid,
            '20000000-0000-0000-0000-000000000104'::uuid,
            '20000000-0000-0000-0000-000000000105'::uuid,
            '20000000-0000-0000-0000-000000000201'::uuid,
            '20000000-0000-0000-0000-000000000202'::uuid,
            '20000000-0000-0000-0000-000000000203'::uuid,
            '20000000-0000-0000-0000-000000000204'::uuid,
            '20000000-0000-0000-0000-000000000205'::uuid,
            '20000000-0000-0000-0000-000000000301'::uuid,
            '20000000-0000-0000-0000-000000000302'::uuid,
            '20000000-0000-0000-0000-000000000303'::uuid,
            '20000000-0000-0000-0000-000000000304'::uuid,
            '20000000-0000-0000-0000-000000000401'::uuid,
            '20000000-0000-0000-0000-000000000402'::uuid,
            '20000000-0000-0000-0000-000000000403'::uuid,
            '20000000-0000-0000-0000-000000000404'::uuid,
            '20000000-0000-0000-0000-000000000501'::uuid,
            '20000000-0000-0000-0000-000000000502'::uuid,
            '20000000-0000-0000-0000-000000000503'::uuid,
            '20000000-0000-0000-0000-000000000504'::uuid,
            '20000000-0000-0000-0000-000000000601'::uuid,
            '20000000-0000-0000-0000-000000000602'::uuid,
            '20000000-0000-0000-0000-000000000603'::uuid,
            '20000000-0000-0000-0000-000000000604'::uuid
        ] AS category_ids
),
insert_listing_category AS (
    INSERT INTO listing_category (listing_id, category_id)
    SELECT li.id,
           (SELECT category_ids[((li.idx - 1) % array_length(category_ids, 1)) + 1] FROM category_arrays)
    FROM listing_index li
    ON CONFLICT DO NOTHING
),
insert_slot_1 AS (
    INSERT INTO availability_slot (id, listing_id, starts_at, ends_at, note)
    SELECT
        gen_random_uuid(),
        li.id,
        (CURRENT_DATE + (li.idx % 10) * INTERVAL '1 day')::timestamptz + TIME '09:00',
        (CURRENT_DATE + (li.idx % 10) * INTERVAL '1 day')::timestamptz + TIME '18:00',
        'Свободно весь день'
    FROM listing_index li
)
SELECT 1;

COMMIT;
