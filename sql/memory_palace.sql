CREATE EXTENSION IF NOT EXISTS pg_trgm;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS email VARCHAR(128),
    ADD COLUMN IF NOT EXISTS bio VARCHAR(256),
    ADD COLUMN IF NOT EXISTS source_language VARCHAR(16) NOT NULL DEFAULT 'zh-CN',
    ADD COLUMN IF NOT EXISTS target_language VARCHAR(16) NOT NULL DEFAULT 'en',
    ADD COLUMN IF NOT EXISTS ui_language VARCHAR(16) NOT NULL DEFAULT 'zh-CN',
    ADD COLUMN IF NOT EXISTS theme_mode VARCHAR(16) NOT NULL DEFAULT 'system';

CREATE UNIQUE INDEX IF NOT EXISTS uk_users_email_not_null
    ON users(email)
    WHERE email IS NOT NULL AND email <> '';

DROP TABLE IF EXISTS ar_scan_words;
DROP TABLE IF EXISTS memory_palace_words;
DROP TABLE IF EXISTS memory_palaces;

CREATE TABLE memory_palaces (
    id BIGINT PRIMARY KEY,
    user_pk_id BIGINT NOT NULL,
    scene_key VARCHAR(128) NOT NULL,
    scene_name VARCHAR(128) NOT NULL,
    cover_url VARCHAR(512),
    word_count INTEGER NOT NULL DEFAULT 0,
    sort_order INTEGER NOT NULL DEFAULT 0,
    create_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    update_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_memory_palaces_user_scene UNIQUE (user_pk_id, scene_key)
);

CREATE INDEX idx_memory_palaces_user_update
    ON memory_palaces(user_pk_id, update_time DESC, id DESC);
CREATE INDEX idx_memory_palaces_user_sort
    ON memory_palaces(user_pk_id, sort_order ASC, update_time DESC, id DESC);
CREATE INDEX idx_memory_palaces_scene_name_trgm
    ON memory_palaces USING gin (scene_name gin_trgm_ops);

CREATE TABLE memory_palace_words (
    id BIGINT PRIMARY KEY,
    palace_id BIGINT NOT NULL,
    user_pk_id BIGINT NOT NULL,
    word_asset_id BIGINT NOT NULL,
    anchor_x_percent INTEGER,
    anchor_y_percent INTEGER,
    source_image_url VARCHAR(512),
    create_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    update_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_memory_palace_words_palace_asset UNIQUE (user_pk_id, palace_id, word_asset_id)
);

CREATE INDEX idx_memory_palace_words_user
    ON memory_palace_words(user_pk_id);
CREATE INDEX idx_memory_palace_words_asset
    ON memory_palace_words(word_asset_id);
CREATE INDEX idx_memory_palace_words_palace_create
    ON memory_palace_words(user_pk_id, palace_id, create_time DESC, id DESC);
CREATE INDEX idx_memory_palace_words_anchor
    ON memory_palace_words(user_pk_id, anchor_x_percent, anchor_y_percent);

CREATE TABLE ar_scan_words (
    id BIGINT PRIMARY KEY,
    user_pk_id BIGINT NOT NULL,
    word_asset_id BIGINT NOT NULL,
    source_image_url VARCHAR(512),
    scene_name VARCHAR(128),
    first_palace_id BIGINT,
    create_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    update_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_ar_scan_words_user_asset UNIQUE (user_pk_id, word_asset_id)
);

CREATE INDEX idx_ar_scan_words_user_update
    ON ar_scan_words(user_pk_id, update_time DESC, id DESC);
CREATE INDEX idx_ar_scan_words_asset
    ON ar_scan_words(word_asset_id);
CREATE INDEX idx_ar_scan_words_scene_name_trgm
    ON ar_scan_words USING gin (scene_name gin_trgm_ops);
