CREATE EXTENSION IF NOT EXISTS pg_trgm;

DROP TABLE IF EXISTS word_asset_translations;
DROP TABLE IF EXISTS word_asset_localizations;
DROP TABLE IF EXISTS word_assets;

CREATE TABLE word_assets (
    id BIGINT PRIMARY KEY,
    word_key VARCHAR(128) NOT NULL,
    canonical_word VARCHAR(128) NOT NULL,
    language_payloads TEXT NOT NULL,
    asset_image_url VARCHAR(512),
    asset_source VARCHAR(32) NOT NULL DEFAULT 'llm',
    create_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    update_time TIMESTAMPTZ NOT NULL DEFAULT now(),
    CONSTRAINT uk_word_assets_word_key UNIQUE (word_key),
    CONSTRAINT ck_word_assets_language_payloads_json CHECK (jsonb_typeof(language_payloads::jsonb) = 'array')
);

CREATE INDEX idx_word_assets_word_key
    ON word_assets(word_key);
CREATE INDEX idx_word_assets_word_key_trgm
    ON word_assets USING gin (word_key gin_trgm_ops);
CREATE INDEX idx_word_assets_canonical_word_trgm
    ON word_assets USING gin (canonical_word gin_trgm_ops);
CREATE INDEX idx_word_assets_language_payloads_trgm
    ON word_assets USING gin (language_payloads gin_trgm_ops);

COMMENT ON TABLE word_assets IS 'One conceptual word asset. language_payloads stores per-language word, phonetic, pos, explanation, example, and pronunciationPath.';
COMMENT ON COLUMN word_assets.language_payloads IS 'JSON array. Every language item must use the unified phonetic key for English phonetics, Chinese pinyin, and Japanese romaji.';
