INSERT INTO app_users (id, first_name, last_name, email, username, password, enabled, created_at, updated_at) VALUES
(1, 'Admin', 'Digital ATM', 'admin@demo.com', 'admin', '$2y$10$fLX49ztTRiSBNoTZBxP/T./vicAqJOB3i35NRqOZImgBLNq6osMCC', true, now(), now()),
(2, 'Victor', 'Diaz', 'yordi@demo.com', 'yordi', '$2y$10$YEtOpgs65iAOTInSc6u9huhmjOiIiW9OafS1gMsYAlKimRFgTIFnS', true, now(), now())
ON CONFLICT (username) DO NOTHING;

INSERT INTO user_roles (user_id, role_id) VALUES
(1, 1),
(1, 2),
(2, 1)
ON CONFLICT DO NOTHING;

INSERT INTO bank_accounts (id, account_number, type, status, balance, owner_id, failed_pin_attempts, created_at, updated_at) VALUES
(1, '100000000001', 'SAVINGS', 'ACTIVE', 2500.00, 2, 0, now(), now()),
(2, '100000000002', 'CHECKING', 'ACTIVE', 980.00, 2, 0, now(), now()),
(3, '100000000003', 'SAVINGS', 'ACTIVE', 10000.00, 1, 0, now(), now())
ON CONFLICT (account_number) DO NOTHING;

INSERT INTO cards (id, card_number, pin_hash, status, account_id, created_at, updated_at) VALUES
(1, '4000001234567890', '$2y$10$bsfDMARCDxl9xmHen9EdE.O2WSmjvdWRoQsAAcAkT2CdY2r3lv02.', 'ACTIVE', 1, now(), now()),
(2, '4000009999888877', '$2y$10$bsfDMARCDxl9xmHen9EdE.O2WSmjvdWRoQsAAcAkT2CdY2r3lv02.', 'ACTIVE', 2, now(), now())
ON CONFLICT (card_number) DO NOTHING;

INSERT INTO transactions (id, type, status, amount, source_account_id, target_account_id, transaction_code, idempotency_key, created_by_username, description, created_at) VALUES
(1, 'DEPOSIT', 'SUCCESS', 2500.00, null, 1, 'TXN-DEMO-000001', 'demo-seed-001', 'yordi', 'Depósito demo inicial', now())
ON CONFLICT (transaction_code) DO NOTHING;

INSERT INTO audit_logs (id, username, action, result, ip, created_at) VALUES
(1, 'system', 'DEMO_DATA_LOADED', 'SUCCESS', null, now())
ON CONFLICT DO NOTHING;

SELECT setval(pg_get_serial_sequence('app_users', 'id'), 2, true);
SELECT setval(pg_get_serial_sequence('bank_accounts', 'id'), 3, true);
SELECT setval(pg_get_serial_sequence('cards', 'id'), 2, true);
SELECT setval(pg_get_serial_sequence('transactions', 'id'), 1, true);
SELECT setval(pg_get_serial_sequence('audit_logs', 'id'), 1, true);
