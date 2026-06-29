CREATE TABLE IF NOT EXISTS users
(
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(250) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);

CREATE TABLE IF NOT EXISTS categories
(
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(50) NOT NULL UNIQUE
);

CREATE TABLE IF NOT EXISTS events
(
    id                 BIGSERIAL PRIMARY KEY,
    title              VARCHAR(120),
    annotation         VARCHAR(2000),
    description        VARCHAR(7000),
    event_date         TIMESTAMP,
    initiator_id       BIGINT REFERENCES users (id),
    category_id        BIGINT REFERENCES categories (id),
    lat                DOUBLE PRECISION,
    lon                DOUBLE PRECISION,
    paid               BOOLEAN,
    participant_limit  INT,
    request_moderation BOOLEAN,
    status             VARCHAR(20) CHECK (status IN ('PENDING', 'PUBLISHED', 'CANCELED')),
    created_on         TIMESTAMP,
    published_on       TIMESTAMP,
    confirmed_requests BIGINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_events_initiator ON events(initiator_id);
CREATE INDEX IF NOT EXISTS idx_events_category ON events(category_id);
CREATE INDEX IF NOT EXISTS idx_events_status ON events(status);

CREATE TABLE IF NOT EXISTS requests
(
    id           BIGSERIAL PRIMARY KEY,
    created      TIMESTAMP,
    event_id     BIGINT REFERENCES events (id),
    requester_id BIGINT REFERENCES users (id),
    status       VARCHAR(20) CHECK (status IN ('PENDING', 'CONFIRMED', 'REJECTED', 'CANCELED')),
    UNIQUE (event_id, requester_id)
);

CREATE INDEX IF NOT EXISTS idx_requests_event ON requests(event_id);
CREATE INDEX IF NOT EXISTS idx_requests_requester ON requests(requester_id);
CREATE INDEX IF NOT EXISTS idx_requests_status ON requests(status);
