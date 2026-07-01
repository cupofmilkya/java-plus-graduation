CREATE DATABASE ewm_users;
CREATE USER postgres WITH PASSWORD 'postgres';
GRANT ALL PRIVILEGES ON DATABASE ewm_users TO postgres;

DROP TABLE IF EXISTS users;

CREATE TABLE users
(
    id    BIGSERIAL PRIMARY KEY,
    name  VARCHAR(250) NOT NULL,
    email VARCHAR(254) NOT NULL UNIQUE
);

CREATE INDEX idx_users_email ON users(email);