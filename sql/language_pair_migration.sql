-- The word asset model is ID-first and single-row-per-concept:
-- word_assets.language_payloads stores all language-specific payloads as JSON.
-- Keep this file as a harmless preference-default migration for older setups.

ALTER TABLE users
    ALTER COLUMN source_language SET DEFAULT 'zh-CN';

ALTER TABLE users
    ALTER COLUMN target_language SET DEFAULT 'en';

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS ui_language VARCHAR(16) NOT NULL DEFAULT 'zh-CN';
