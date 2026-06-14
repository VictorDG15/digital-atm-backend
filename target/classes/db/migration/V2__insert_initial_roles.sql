INSERT INTO roles (id, name) VALUES
(1, 'USER'),
(2, 'ADMIN')
ON CONFLICT (name) DO NOTHING;

SELECT setval(pg_get_serial_sequence('roles', 'id'), 2, true);
