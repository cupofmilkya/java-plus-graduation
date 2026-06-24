-- Таблица заявок на участие
CREATE TABLE IF NOT EXISTS requests
(
    id           BIGSERIAL PRIMARY KEY,
    event_id     BIGINT,
    requester_id BIGINT,
    status       TEXT CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELED')),
    created      TIMESTAMP
);
