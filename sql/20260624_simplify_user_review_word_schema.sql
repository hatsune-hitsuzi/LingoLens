BEGIN;

ALTER TABLE users
    ADD COLUMN IF NOT EXISTS source_language VARCHAR(16) NOT NULL DEFAULT 'zh-CN',
    ADD COLUMN IF NOT EXISTS target_language VARCHAR(16) NOT NULL DEFAULT 'en',
    ADD COLUMN IF NOT EXISTS ui_language VARCHAR(16) NOT NULL DEFAULT 'zh-CN',
    ADD COLUMN IF NOT EXISTS theme_mode VARCHAR(16) NOT NULL DEFAULT 'system';

DO $$
BEGIN
    IF to_regclass('public.user_preferences') IS NOT NULL THEN
        UPDATE users AS app_user
        SET source_language = COALESCE(pref.source_language, app_user.source_language, 'zh-CN'),
            target_language = COALESCE(pref.target_language, app_user.target_language, 'en'),
            ui_language = COALESCE(pref.ui_language, app_user.ui_language, pref.source_language, 'zh-CN'),
            theme_mode = COALESCE(pref.theme_mode, app_user.theme_mode, 'system'),
            update_time = now()
        FROM user_preferences AS pref
        WHERE pref.user_pk_id = app_user.id;
    END IF;
END $$;

ALTER TABLE memory_palace_words
    ADD COLUMN IF NOT EXISTS anchor_x_percent INTEGER,
    ADD COLUMN IF NOT EXISTS anchor_y_percent INTEGER;

DO $$
BEGIN
    IF EXISTS (
        SELECT 1
        FROM information_schema.columns
        WHERE table_schema = 'public'
          AND table_name = 'memory_palace_words'
          AND column_name = 'slot_key'
    ) THEN
        UPDATE memory_palace_words
        SET anchor_x_percent = NULLIF(substring(slot_key FROM '^ar:(\d+):\d+$'), '')::INTEGER,
            anchor_y_percent = NULLIF(substring(slot_key FROM '^ar:\d+:(\d+)$'), '')::INTEGER
        WHERE slot_key ~ '^ar:\d+:\d+$'
          AND (anchor_x_percent IS NULL OR anchor_y_percent IS NULL);
    END IF;
END $$;

ALTER TABLE memory_palace_words
    DROP COLUMN IF EXISTS slot_key,
    DROP COLUMN IF EXISTS slot_name,
    DROP COLUMN IF EXISTS anchor_description,
    DROP COLUMN IF EXISTS image_optimized,
    DROP COLUMN IF EXISTS image_upload_provider;

DROP INDEX IF EXISTS idx_memory_palace_words_slot_key;
CREATE INDEX IF NOT EXISTS idx_memory_palace_words_anchor
    ON memory_palace_words(user_pk_id, anchor_x_percent, anchor_y_percent);

CREATE TABLE IF NOT EXISTS review_word_progress (
    user_pk_id BIGINT NOT NULL,
    word_asset_id BIGINT NOT NULL,
    last_word_id BIGINT,
    last_result SMALLINT NOT NULL DEFAULT 0,
    correct_count INTEGER NOT NULL DEFAULT 0,
    wrong_count INTEGER NOT NULL DEFAULT 0,
    total_count INTEGER NOT NULL DEFAULT 0,
    last_answer_time TIMESTAMPTZ,
    last_review_date DATE,
    create_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    update_time TIMESTAMPTZ NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT pk_review_word_progress PRIMARY KEY (user_pk_id, word_asset_id),
    CONSTRAINT ck_review_word_progress_last_result CHECK (last_result IN (0, 1))
);

INSERT INTO review_word_progress (
    user_pk_id,
    word_asset_id,
    last_word_id,
    last_result,
    correct_count,
    wrong_count,
    total_count,
    last_answer_time,
    last_review_date,
    create_time,
    update_time
)
SELECT DISTINCT ON (latest.user_pk_id, latest.word_asset_id)
    latest.user_pk_id,
    latest.word_asset_id,
    latest.word_id,
    CASE WHEN latest.is_recognized THEN 1 ELSE 0 END,
    stats.correct_count,
    stats.wrong_count,
    stats.total_count,
    latest.answer_time,
    latest.answer_time::date,
    stats.first_create_time,
    now()
FROM review_records AS latest
JOIN (
    SELECT
        user_pk_id,
        word_asset_id,
        SUM(CASE WHEN is_recognized THEN 1 ELSE 0 END)::INTEGER AS correct_count,
        SUM(CASE WHEN is_recognized THEN 0 ELSE 1 END)::INTEGER AS wrong_count,
        COUNT(*)::INTEGER AS total_count,
        MIN(create_time) AS first_create_time
    FROM review_records
    GROUP BY user_pk_id, word_asset_id
) AS stats
  ON stats.user_pk_id = latest.user_pk_id
 AND stats.word_asset_id = latest.word_asset_id
ORDER BY latest.user_pk_id, latest.word_asset_id, latest.answer_time DESC, latest.id DESC
ON CONFLICT (user_pk_id, word_asset_id) DO UPDATE SET
    last_word_id = EXCLUDED.last_word_id,
    last_result = EXCLUDED.last_result,
    correct_count = EXCLUDED.correct_count,
    wrong_count = EXCLUDED.wrong_count,
    total_count = EXCLUDED.total_count,
    last_answer_time = EXCLUDED.last_answer_time,
    last_review_date = EXCLUDED.last_review_date,
    update_time = now();

CREATE INDEX IF NOT EXISTS idx_review_word_progress_user_day
    ON review_word_progress(user_pk_id, last_review_date DESC, update_time DESC);

ALTER TABLE word_assets
    DROP COLUMN IF EXISTS pronunciation_base_url,
    DROP COLUMN IF EXISTS canonical_language;

DROP TABLE IF EXISTS user_preferences;

COMMIT;
