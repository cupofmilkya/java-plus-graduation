-- Таблица категорий
CREATE TABLE IF NOT EXISTS categories
(
    id   BIGSERIAL PRIMARY KEY,
    name TEXT NOT NULL UNIQUE
);

-- Таблица событий
CREATE TABLE IF NOT EXISTS events
(
    id                 BIGSERIAL PRIMARY KEY,
    title              TEXT,
    annotation         TEXT,
    description        TEXT,
    event_date         TIMESTAMP,
    initiator_id       BIGINT,
    category_id        BIGINT REFERENCES categories (id),
    paid               BOOLEAN,
    participant_limit  INT,
    request_moderation BOOLEAN,
    status             TEXT CHECK (status IN ('PENDING', 'PUBLISHED', 'CANCELED')),
    created_on         TIMESTAMP,
    published_on       TIMESTAMP,
    confirmed_requests BIGINT DEFAULT 0
);
