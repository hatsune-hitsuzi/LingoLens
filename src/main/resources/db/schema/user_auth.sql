CREATE SEQUENCE IF NOT EXISTS user_id_seq
    START WITH 1
    INCREMENT BY 1
    MINVALUE 1
    NO MAXVALUE
    CACHE 1;

CREATE TABLE IF NOT EXISTS users (
    id BIGINT PRIMARY KEY,
    user_id CHAR(8) NOT NULL UNIQUE,
    nickname VARCHAR(32) NOT NULL UNIQUE,
    avatar_url TEXT NOT NULL,
    email VARCHAR(128),
    bio VARCHAR(256),
    password_hash VARCHAR(128) NOT NULL,
    create_time TIMESTAMPTZ,
    update_time TIMESTAMPTZ
);

CREATE INDEX IF NOT EXISTS idx_users_nickname ON users (nickname);
CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email_not_null
    ON users(email)
    WHERE email IS NOT NULL AND email <> '';
