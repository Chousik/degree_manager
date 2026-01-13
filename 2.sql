BEGIN;

-- Users
INSERT INTO "user" (id, email, username, name, surname, last_name, phone, rating, status, city, created_at)
VALUES ('11111111-1111-1111-1111-111111111111', 'admin@fixly.local', 'admin', 'Admin', 'Fixly', NULL, NULL, 5.0,
        'ACTIVE', 'Москва', NOW()),
       ('22222222-2222-2222-2222-222222222222', 'user1@fixly.local', 'user1', 'Иван', 'Петров', NULL, '+79990000001',
        4.5, 'ACTIVE', 'Москва', NOW()),
       ('33333333-3333-3333-3333-333333333333', 'user2@fixly.local', 'user2', 'Ольга', 'Смирнова', NULL, '+79990000002',
        4.2, 'ACTIVE', 'СПб', NOW()),
       ('44444444-4444-4444-4444-444444444444', 'user3@fixly.local', 'user3', 'Дмитрий', 'Орлов', NULL, '+79990000003',
        4.8, 'ACTIVE', 'Казань', NOW());

-- Listings (flagged)
INSERT INTO listing (id, owner_id, title, description, price_per_hour, deposit_amount, auto_confirmation, status, latitude, longitude, created_at, is_flagged, flag_reason)
VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', 'Перфоратор Bosch', 'Сильный, но цена указана неверно', 350.00, 2000.00, true, 'AVAILABLE', 55.7558, 37.6173, NOW() - INTERVAL '2 days', true, 'Цена в описании не совпадает'),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-3333-3333-3333-333333333333', 'Пила Makita', 'Состояние сомнительное', 280.00, 1500.00, false, 'AVAILABLE', 59.9386, 30.3141, NOW() - INTERVAL '1 day', true, 'Подозрение на неисправность');

-- Listing photos (MinIO: http://localhost:9000/media/)
INSERT INTO listing_photo (listing_id, url, sort_order)
VALUES
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'http://localhost:8075/media/232.jpg', 1),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'http://localhost:8075/media/carpentry-construction-collage-tools-underneath-wooden-planks_488220-29813.jpg', 2),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'http://localhost:8075/media/tools_909293-2254.jpg', 3),
  ('aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'http://localhost:8075/media/top-view-mechanical-tools-arrangement_23-2149552411.jpg', 4),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'http://localhost:8075/media/work-tool-blue-background-hand-tool-new-set-repair-construction-overhead_771335-2453.jpg', 1),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'http://localhost:8075/media/work-tools-on-the-wooden-background_220873-8575.jpg', 2),
  ('bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'http://localhost:8075/media/working-tools-repair-isolated-white_290947-962.jpg', 3);

-- Rentals
INSERT INTO rental (id, listing_id, lessor_id, lessee_id, start_at, end_at, status, total_amount)
VALUES
  ('aaaaaaaa-1111-1111-1111-aaaaaaaa1111', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', NOW() - INTERVAL '7 days', NOW() - INTERVAL '5 days', 'COMPLETED', 700.00),
  ('bbbbbbbb-1111-1111-1111-bbbbbbbb1111', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', '33333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444', NOW() - INTERVAL '3 days', NOW() - INTERVAL '2 days', 'COMPLETED', 560.00);

-- Reviews (flagged)
INSERT INTO review (id, lessor_id, lessee_id, listing_id, rental_id, author_role, rating, text, created_at, is_flagged, flag_reason)
VALUES
  ('cccccccc-cccc-cccc-cccc-cccccccccccc', '22222222-2222-2222-2222-222222222222', '33333333-3333-3333-3333-333333333333', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'aaaaaaaa-1111-1111-1111-aaaaaaaa1111', 'LESSEE', 2, 'Очень грубое общение, угрозы.', NOW() - INTERVAL '1 day', true, 'Оскорбления'),
  ('dddddddd-dddd-dddd-dddd-dddddddddddd', '33333333-3333-3333-3333-333333333333', '44444444-4444-4444-4444-444444444444', 'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb', 'bbbbbbbb-1111-1111-1111-bbbbbbbb1111', 'LESSOR', 1, 'Нецензурная лексика в тексте.', NOW() - INTERVAL '6 hours', true, 'Нецензурная лексика');

-- Support tickets
INSERT INTO support_ticket (id, requester_id, rental_id, status, subject, message, created_at)
VALUES
  ('eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee', '33333333-3333-3333-3333-333333333333', 'aaaaaaaa-1111-1111-1111-aaaaaaaa1111', 'OPEN', 'Спор по залогу', 'Прошу пересмотреть удержание залога.', NOW() - INTERVAL '5 hours'),
  ('ffffffff-ffff-ffff-ffff-ffffffffffff', '44444444-4444-4444-4444-444444444444', 'bbbbbbbb-1111-1111-1111-bbbbbbbb1111', 'IN_PROGRESS', 'Просрочка возврата', 'Инструмент вернул позже, хочу уточнить штраф.', NOW() - INTERVAL '8 hours');

-- Reports (complaints)
INSERT INTO report (id, reporter_id, status, target_type, target_id, reason_body, created_at, resolved_by)
VALUES
  ('11111111-2222-3333-4444-555555555555', '33333333-3333-3333-3333-333333333333', 'OPEN', 'RENTAL',  'aaaaaaaa-1111-1111-1111-aaaaaaaa1111', 'Повреждение инструмента после аренды.', NOW() - INTERVAL '3 hours', NULL),
  ('66666666-7777-8888-9999-000000000000', '44444444-4444-4444-4444-444444444444', 'OPEN', 'RENTAL',  'bbbbbbbb-1111-1111-1111-bbbbbbbb1111', 'Нарушение условий передачи.', NOW() - INTERVAL '2 hours', NULL),
  ('77777777-8888-9999-0000-111111111111', '33333333-3333-3333-3333-333333333333', 'OPEN', 'LISTING', 'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa', 'Цена в описании не совпадает', NOW() - INTERVAL '4 hours', NULL),
  ('88888888-9999-0000-1111-222222222222', '44444444-4444-4444-4444-444444444444', 'OPEN', 'REVIEW',  'cccccccc-cccc-cccc-cccc-cccccccccccc', 'Оскорбления в отзыве', NOW() - INTERVAL '2 hours', NULL);

-- Bans
INSERT INTO ban_list (id, banned_user_id, ban_reason, ban_type, ban_duration, admin_user_id, status, created_at)
VALUES
  ('99999999-aaaa-bbbb-cccc-999999999999', '44444444-4444-4444-4444-444444444444', 'Повторные нарушения правил', 'TEMP', NOW() + INTERVAL '7 days', '11111111-1111-1111-1111-111111111111', 'ACTIVE', NOW() - INTERVAL '1 hour');

UPDATE "user"
SET last_ban = '99999999-aaaa-bbbb-cccc-999999999999',
    status = 'BANNED'
WHERE id = '44444444-4444-4444-4444-444444444444';

COMMIT;
