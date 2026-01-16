BEGIN;

DELETE FROM ban_list WHERE id = '99999999-aaaa-bbbb-cccc-999999999999';
UPDATE "user"
SET last_ban = NULL,
    status = 'ACTIVE'
WHERE id = '44444444-4444-4444-4444-444444444444';

DELETE FROM report WHERE id IN (
  '11111111-2222-3333-4444-555555555555',
  '66666666-7777-8888-9999-000000000000',
  '77777777-8888-9999-0000-111111111111',
  '88888888-9999-0000-1111-222222222222'
);

DELETE FROM support_ticket WHERE id IN (
  'eeeeeeee-eeee-eeee-eeee-eeeeeeeeeeee',
  'ffffffff-ffff-ffff-ffff-ffffffffffff'
);

DELETE FROM review WHERE id IN (
  'cccccccc-cccc-cccc-cccc-cccccccccccc',
  'dddddddd-dddd-dddd-dddd-dddddddddddd'
);

DELETE FROM rental WHERE id IN (
  'aaaaaaaa-1111-1111-1111-aaaaaaaa1111',
  'bbbbbbbb-1111-1111-1111-bbbbbbbb1111'
);

DELETE FROM listing WHERE id IN (
  'aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa',
  'bbbbbbbb-bbbb-bbbb-bbbb-bbbbbbbbbbbb'
);

DELETE FROM "user" WHERE id IN (
  '11111111-1111-1111-1111-111111111111',
  '22222222-2222-2222-2222-222222222222',
  '33333333-3333-3333-3333-333333333333',
  '44444444-4444-4444-4444-444444444444'
);

COMMIT;
