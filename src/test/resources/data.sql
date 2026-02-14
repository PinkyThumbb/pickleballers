INSERT INTO player(name,email,created_at) VALUES
     ('Alice Johnson', 'alice@example.com', '2026-01-01T01:01:01.000000'),
     ('Bob Smith', 'bob@example.com', '2026-01-01T01:01:01.000000'),
     ('Carol Lee', 'carol@example.com', '2026-01-01T01:01:01.000000');

INSERT INTO match(playera_id, playerb_id, score, status, idempotency_key, created_at) VALUES
     (1, 2, '11-9', 'CONFIRMED', 'key1', '2026-01-01T01:01:01.000000');